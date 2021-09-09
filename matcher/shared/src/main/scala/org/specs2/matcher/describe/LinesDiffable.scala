package org.specs2.matcher.describe

/** Import the implicit Diffable contained in this object to get a colored output showing line differences in Strings
  * containing lots of lines
  */
object LinesDiffable:

  given largeStringDiffable: Diffable[String] with
    def diff(actual: String, expected: String): ComparisonResult =
      val (actualLines, expectedLines) =
        (actual.toString.split("\n").toList, expected.toString.split("\n").toList)

      if actualLines.size + expectedLines.size > 2 then linesDiffable[String].diff(actualLines, expectedLines)
      else Diffable.stringDiffable.diff(actual, expected)

  given linesDiffable[T: Diffable]: Diffable[List[T]] with
    def diff(actual: List[T], expected: List[T]): ComparisonResult =
      LinesComparisonResult(actual, expected)

case class LinesComparisonResult[T: Diffable](actual: List[T], expected: List[T]) extends ComparisonResult:
  import org.specs2.data.*
  import EditDistance.*
  import org.specs2.text.AnsiColors.*

  private val diffable: Diffable[T] =
    implicitly[Diffable[T]]

  private lazy val operations: IndexedSeq[EditDistanceOperation[T]] =
    levenhsteinDistance[T](actual.toIndexedSeq, expected.toIndexedSeq)(using
      new Equiv[T]:
        def equiv(a: T, b: T) = diffable.diff(a, b).identical
    )

  def identical: Boolean =
    actual.size == expected.size &&
      actual.zip(expected).forall { case (a, e) => diffable.diff(a, e).identical }

  def render: String = operations
    .flatMap {
      case Same(line)          => List(line)
      case Add(line)           => List(color("+ " + line, green))
      case Del(line)           => List(color("- " + line, red))
      case Subst(line1, line2) => List(color("- " + line1, red), color("+ " + line2, green))
    }
    .mkString("", "\n", "")
