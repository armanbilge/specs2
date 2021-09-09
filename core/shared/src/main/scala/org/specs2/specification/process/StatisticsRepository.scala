package org.specs2
package specification
package process

import fp.*, syntax.*
import control.*
import execute.Result
import io.*
import specification.core.Description
import scala.collection.mutable.HashMap

/** Store the execution statistics.
  *
  * The actual store might be on disk on in-memory
  */
trait StatisticsRepository:
  /** get the latest statistics for a given specification */
  def getStatistics(specClassName: String): Operation[Option[Stats]]

  /** get the latest statistics for a given specification, or return a default value */
  def getStatisticsOr(specClassName: String, stats: Stats): Operation[Stats]

  /** store the final statistics for a given specification */
  def storeStatistics(specClassName: String, stats: Stats): Operation[Unit]

  def storeResult(specClassName: String, description: Description, result: Result): Operation[Unit]

  /** @return the previous executed result of an example */
  def previousResult(specClassName: String, d: Description): Operation[Option[Result]]

  /** remove all previously stored statistics */
  def resetStatistics: Operation[Unit]

case class DefaultStatisticsRepository(store: Store) extends StatisticsRepository:
  /** get the latest statistics for a given specification */
  def getStatistics(specClassName: String): Operation[Option[Stats]] =
    store.get(SpecificationStatsKey(specClassName))

  /** get the latest statistics for a given specification, or return a default value */
  def getStatisticsOr(specClassName: String, stats: Stats): Operation[Stats] =
    getStatistics(specClassName).map(_.getOrElse(stats))

  /** store the final statistics for a given specification */
  def storeStatistics(specClassName: String, stats: Stats): Operation[Unit] =
    store.set(SpecificationStatsKey(specClassName), stats)

  def storeResult(specClassName: String, description: Description, result: Result): Operation[Unit] =
    store.set(SpecificationResultKey(specClassName, description), result)

  /** @return the previous executed result of an example */
  def previousResult(specClassName: String, d: Description): Operation[Option[Result]] =
    store.get(SpecificationResultKey(specClassName, d))

  /** remove all previously stored statistics */
  def resetStatistics: Operation[Unit] =
    store.reset

/** In memory store for statistics
  */
case class StatisticsMemoryStore(
    statistics: HashMap[String, Stats] = new HashMap[String, Stats],
    results: HashMap[(String, Long), Result] = new HashMap[(String, Long), Result]
) extends Store:
  def get[A](key: Key[A]): Operation[Option[A]] = key match
    case SpecificationStatsKey(specClassName) =>
      Operation.ok(statistics.get(specClassName))

    case SpecificationResultKey(specClassName, description) =>
      Operation.ok(results.get((specClassName, description.hashCode.toLong)))

  def set[A](key: Key[A], a: A): Operation[Unit] = key match
    case SpecificationStatsKey(specClassName) =>
      Operation.ok(statistics.put(specClassName, a)).map(_ => ())

    case SpecificationResultKey(specClassName, description) =>
      Operation.ok(results.put((specClassName, description.hashCode.toLong), a)).map(_ => ())

  def reset: Operation[Unit] = Operation.ok {
    statistics.clear
    results.clear
  }

case class SpecificationStatsKey(specClassName: String) extends Key[Stats]
case class SpecificationResultKey(specClassName: String, description: Description) extends Key[Result]
