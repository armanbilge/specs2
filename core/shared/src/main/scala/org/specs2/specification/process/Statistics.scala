package org.specs2
package specification
package process

import fp.*, syntax.*
import control.*
import origami.*
import specification.core.*
import concurrent.ExecutionEnv
import producer.*
import Producer.*
import main.Arguments

/** Compute the statistics for executed fragments
  */
trait Statistics:

  /** read the stats for each Fragment of the specifications if the 'was' argument is passed on the command line to
    * query the previous status of a Fragments
    */
  def readStats(spec: SpecStructure): SpecStructure

  /** read the stats for one Fragment
    */
  def readStats(className: String)(fragment: Fragment): Operation[Fragment]

case class DefaultStatistics(arguments: Arguments, statisticsRepository: StatisticsRepository) extends Statistics:

  def readStats(spec: SpecStructure): SpecStructure =
    // we need to use the arguments passed on the command line and override them with the spec arguments
    val args = arguments.overrideWith(spec.arguments)

    if args.wasIsDefined then spec.flatMap(f => eval[Action, Fragment](readStats(spec.specClassName)(f).toAction))
    else spec

  /** read the stats for one Fragment from the statistics repository
    */
  def readStats(className: String)(fragment: Fragment): Operation[Fragment] =
    statisticsRepository.previousResult(className, fragment.description).map(fragment.setPreviousResult)

object Statistics:

  /** get the stats for each fragment of a specification */
  def statsProcess: AsyncTransducer[Fragment, Stats] =
    (p: AsyncStream[Fragment]) => p.reduceMapEval(_.executionResult.map(Stats.apply))

  /** collect all the stats for a specification */
  def runStats(spec: SpecStructure)(ee: ExecutionEnv): Stats =
    spec.contents.fold(fold).runMonoid(ee)

  /** Fold for collecting stats */
  def fold: AsyncFold[Fragment, Stats] { type S = Stats } =
    Folds.fromMonoidMapEval(fromFragment)

  /** create an empty Stats object for a given fragment, counting 1 for an example */
  def emptyStats(fragment: Fragment): Stats =
    if Fragment.isExample(fragment) then Stats(examples = 1)
    else Stats.empty

  def fromFragment(fragment: Fragment): Action[Stats] =
    if fragment.isExecutable then
      fragment.executedResult.map { case ExecutedResult(result, timer) =>
        emptyStats(fragment).withResult(result).copy(timer = timer)
      }
    else Action.pure(Stats.empty)
