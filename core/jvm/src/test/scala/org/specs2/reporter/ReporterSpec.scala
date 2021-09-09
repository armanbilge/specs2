package org.specs2
package reporter

import control.*
import matcher.*
import execute.*
import main.Arguments
import PrinterLogger.*
import specification.create.S2StringContext
import specification.dsl.FragmentsDsl
import specification.core.*
import specification.process.*
import OperationMatchers.*
import org.specs2.control.origami.Folds

class ReporterSpec(val env: Env) extends Specification with ThrownExpectations with OwnEnv {
  def is = s2"""

 The reporter is responsible for:
   - filtering the specification fragments
   - executing the specification
   - running various printers
   - saving the specification state

 A specification is
   filtered $a1

 And at the end of the reporting
   the spec stats are saved $a2
   the example stats are saved $a3
   the stats are returned $a4

 Different printers can be used with the reporter
   by default the text printer is used $b1
   other printers (then printer is not used) $b2
   unless console is in the arguments $b3

"""

  import reporterSpecSupport.*

  def a1 =
    val logger = stringPrinterLogger
    reported(ownEnv.setArguments(Arguments.split("ex ex3")), logger)
    logger.messages.mkString("\n") must contain("ex3")
    logger.messages.mkString("\n") must not(contain("ex1"))

  def a2 =
    val repository = StatisticsRepositoryCreation.memory
    reported(ownEnv.setArguments(Arguments()).setStatisticRepository(repository))
    repository.getStatistics(spec().specClassName) must beOk(beSome((_: Stats).examples must ===(3)))

  def a3 =
    val repository = StatisticsRepositoryCreation.memory
    reported(ownEnv.setArguments(Arguments()).setStatisticRepository(repository))
    val ex2 = spec().fragmentsList(env.executionEnv)(3)
    repository.previousResult(spec().specClassName, ex2.description) must beOk(
      beSome((_: Result).isFailure must beTrue)
    )

  def a4 =
    reported(ownEnv).map(_.copy(timer = Stats.empty.timer)) must beSome(
      Stats(examples = 3, successes = 2, expectations = 3, failures = 1)
    )

  def b1 =
    val logger = stringPrinterLogger
    reported(ownEnv.setPrinterLogger(logger), logger)
    logger.messages must not(beEmpty)

  def b2 =
    val logger = stringPrinterLogger
    reported(
      ownEnv.setPrinterLogger(logger).setArguments(Arguments("junit")),
      printers = List(new FakeJUnitPrinter(logger))
    )
    logger.messages must not(contain[String]("ex1"))
    logger.messages must contain("[info] junit")

  def b3 =
    val logger = stringPrinterLogger
    val env = ownEnv.setPrinterLogger(logger).setArguments(Arguments.split("console junit"))

    reported(env, printers = List(TextPrinter(env), new FakeJUnitPrinter(logger)))

    val messages = logger.messages
    messages must contain(beMatching(".*ex1.*"))
    messages must contain("[info] reporterSpecSupport")

}

class FakeJUnitPrinter(logger: PrinterLogger) extends Printer:
  def prepare(specifications: List[SpecStructure]): Action[Unit] = Action.unit
  def finalize(specifications: List[SpecStructure]): Action[Unit] = Action.unit

  def sink(spec: SpecStructure) =
    Folds.fromSink((f: Fragment) => Action.pure(logger.infoLog("junit\n")))

object reporterSpecSupport extends MustMatchers with ExpectedResults with S2StringContext with FragmentsDsl:
  /** TEST METHODS
    */

  def spec(logger: PrinterLogger = NoPrinterLogger): SpecStructure = s2"""
 ex1 ${ex1(logger)}
 ex2 ${ex2(logger)}
 ex3 ${ex3(logger)}
 """

  def ex1(logger: PrinterLogger) = { Thread.sleep(200); logger.infoLog(" e1\n "); ok }
  def ex2(logger: PrinterLogger) = { logger.infoLog("e2\n "); ko }
  def ex3(logger: PrinterLogger) = { logger.infoLog("e3\n "); ok }

  def reported(env: Env, logger: PrinterLogger = NoPrinterLogger, printers: List[Printer] = Nil) =
    val printers1 = if printers.isEmpty then List(TextPrinter(env.setPrinterLogger(logger))) else printers
    val reporter = Reporter.create(printers1, env.copy(printerLogger = NoPrinterLogger, systemLogger = NoLogger))
    reporter.report(spec(logger)).runOption(env.executionEnv)

  def indexOf(messages: Seq[String])(f: String => Boolean): Int =
    messages.zipWithIndex.find { case (s, i) => f(s) }.fold(-1)(_._2)

  def indexOf(messages: Seq[String], element: String): Int =
    indexOf(messages)((_: String).contains(element))
