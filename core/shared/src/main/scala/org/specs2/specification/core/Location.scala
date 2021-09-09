package org.specs2
package specification
package core

import control.*
import quoted.*

/** Location of a Fragment
  *
  * This is currently implemented using stacktraces which is very brittle
  */
trait Location:
  /** the file path */
  def path: String

  /** the file path */
  def fileName: String =
    path.split("/").last

  /** the line number */
  def lineNumber: Int

  /** the column number */
  def columnNumber: Int

  /** display this location */
  def show: String

/** Location created from a Tasty position
  */
case class PositionLocation(path: String, lineNumber: Int, columnNumber: Int) extends Location:
  def show: String =
    s"""$path (line: $lineNumber, column: $columnNumber)"""

object PositionLocation:

  given ToExpr[PositionLocation] with
    def apply(location: PositionLocation)(using qctx: Quotes): Expr[PositionLocation] = {
      location match
        case PositionLocation(path, line, column) =>
          val pathExpr: Expr[String] = Expr(path)
          val lineExpr: Expr[Int] = Expr(line)
          val columnExpr: Expr[Int] = Expr(column)
          Expr.betaReduce('{ PositionLocation($pathExpr, $lineExpr, $columnExpr) })
    }

case class StacktraceLocation(trace: Seq[StackTraceElement] = (new Exception).getStackTrace.toIndexedSeq)
    extends Location:
  def path: String =
    traceLocation(DefaultStackTraceFilter).map(_.path).getOrElse("no path")

  def lineNumber: Int =
    traceLocation(DefaultStackTraceFilter).map(_.lineNumber).getOrElse(0)

  def columnNumber: Int =
    0

  def traceLocation(filter: StackTraceFilter): Option[TraceLocation] =
    filter(trace).headOption.map(TraceLocation.apply)

  /** @return a filtered Location */
  def filter(filter: StackTraceFilter) = copy(filter(trace))

  def show: String =
    s"${getClass.getSimpleName}(${traceLocation(DefaultStackTraceFilter)}})"
