package org.specs2
package reporter

import control.{Logger as _, *}
import origami.*
import execute.Details
import sbt.testing.*
import text.AnsiColors.*
import text.NotNullStrings.*
import main.Arguments
import specification.core.*
import SbtPrinter.*

/** Text printer for Sbt
  *
  * It delegates the console printing to a normal text printer but using the Sbt loggers It also publishes events
  * (success, error, skipped, pending) to Sbt
  */
case class SbtPrinter(env: Env, loggers: Array[Logger], events: SbtEvents) extends Printer:
  def prepare(specifications: List[SpecStructure]): Action[Unit] = Action.unit
  def finalize(specifications: List[SpecStructure]): Action[Unit] = Action.unit

  lazy val textPrinter = TextPrinter(env.setPrinterLogger(SbtPrinterLogger(loggers)))

  def sbtNotifierPrinter(args: Arguments): Printer =
    NotifierPrinter(args).printer(sbtNotifier(events, args))

  /** use 2 Folds:
    *   - one for logging messages to the console
    *   - one for registering sbt events
    */
  def sink(spec: SpecStructure): AsyncSink[Fragment] =
    textSink(spec) <* eventSink(spec)

  def textSink(spec: SpecStructure): AsyncSink[Fragment] =
    textPrinter.sink(spec)

  def eventSink(spec: SpecStructure): AsyncSink[Fragment] =
    sbtNotifierPrinter(env.arguments).sink(spec)

object SbtPrinter:
  def sbtNotifier(events: SbtEvents, args: Arguments) = new Notifier {
    private val context: scala.collection.mutable.Stack[String] =
      new scala.collection.mutable.Stack[String]

    private def inContext(name: String): String =
      (name +: context.toVector).reverse.mkString("::")

    def specStart(title: String, location: String): Unit = ()
    def specEnd(title: String, location: String): Unit = ()

    def contextStart(text: String, location: String): Unit = { context.push(text); () }

    def contextEnd(text: String, location: String): Unit = { if !context.isEmpty then context.pop; () }

    def text(text: String, location: String): Unit = ()
    def exampleStarted(name: String, location: String): Unit = ()

    def exampleSuccess(name: String, duration: Long): Unit =
      events.succeeded(inContext(name), duration)

    def exampleFailure(
        name: String,
        message: String,
        location: String,
        f: Throwable,
        details: Details,
        duration: Long
    ): Unit =
      events.failure(inContext(name), duration, args.traceFilter(f))

    def exampleError(name: String, message: String, location: String, f: Throwable, duration: Long): Unit =
      events.error(inContext(name), duration, args.traceFilter(f))

    def exampleSkipped(name: String, message: String, location: String, duration: Long): Unit =
      events.skipped(inContext(name), duration)

    def examplePending(name: String, message: String, location: String, duration: Long): Unit =
      events.pending(inContext(name), duration)

    def stepStarted(location: String) = ()
    def stepSuccess(duration: Long) = ()
    def stepError(message: String, location: String, f: Throwable, duration: Long) =
      events.error("step", duration, args.traceFilter(f))

  }

/** Sbt events for a given TaskDef and event handler
  */
trait SbtEvents:
  /** sbt event handler to notify of successes/failures */
  def handler: EventHandler

  /** sbt task definition for this run */
  def taskDef: TaskDef

  def suiteError() = handler.handle(SpecSuiteEvent(Status.Error))
  def suiteError(exception: Throwable) = handler.handle(SpecSuiteEvent(Status.Error, new OptionalThrowable(exception)))

  def error(name: String, durationInMillis: Long, exception: Throwable) =
    handler.handle(SpecTestEvent(name, Status.Error, Some(durationInMillis), new OptionalThrowable(exception)))
  def failure(name: String, durationInMillis: Long, exception: Throwable) =
    handler.handle(SpecTestEvent(name, Status.Failure, Some(durationInMillis), new OptionalThrowable(exception)))
  def succeeded(name: String, durationInMillis: Long) =
    handler.handle(SpecTestEvent(name, Status.Success, Some(durationInMillis)))
  def skipped(name: String, durationInMillis: Long) =
    handler.handle(SpecTestEvent(name, Status.Skipped, Some(durationInMillis)))
  def pending(name: String, durationInMillis: Long) =
    handler.handle(SpecTestEvent(name, Status.Pending, Some(durationInMillis)))
  def ignored(name: String, durationInMillis: Long) =
    handler.handle(SpecTestEvent(name, Status.Ignored, Some(durationInMillis)))
  def canceled(name: String) = handler.handle(SpecTestEvent(name, Status.Canceled, None))

  case class SpecTestEvent(
      name: String,
      status: Status,
      durationInMillis: Option[Long],
      throwable: OptionalThrowable = new OptionalThrowable
  ) extends Event:
    val fullyQualifiedName = taskDef.fullyQualifiedName
    val fingerprint = taskDef.fingerprint
    val selector = new TestSelector(name)
    val duration = durationInMillis.getOrElse(-1L)

  case class SpecSuiteEvent(status: Status, throwable: OptionalThrowable = new OptionalThrowable) extends Event:
    val fullyQualifiedName = taskDef.fullyQualifiedName
    val fingerprint = taskDef.fingerprint
    val selector = new SuiteSelector
    val duration = -1L

/** Line logger using sbt's loggers
  */
case class SbtPrinterLogger(loggers: Array[Logger]) extends BufferedPrinterLogger:
  def infoLine(msg: String) = loggers.foreach { logger =>
    // use a non-empty character when the message is empty otherwise
    // nothing is printed out
    if msg.isEmpty then logger.info(" ")
    else logger.info(removeColors(msg, !logger.ansiCodesSupported))
  }

  /** failures are represented as errors in sbt */
  def failureLine(msg: String) = errorLine(msg)

  def errorLine(msg: String) = loggers.foreach { logger =>
    logger.error(makeLogMessage(msg, logger))
  }

  def warnLine(msg: String) = loggers.foreach { logger =>
    logger.warn(makeLogMessage(msg, logger))
  }

  def makeLogMessage(msg: String, logger: Logger): String =
    // use a non-empty character when the message is empty otherwise
    // nothing is printed out
    if msg == null || msg.isEmpty then " "
    else removeColors(msg.notNull, !logger.ansiCodesSupported)
