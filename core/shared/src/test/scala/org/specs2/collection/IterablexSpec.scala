package org.specs2
package collection

import mutable.Specification
import org.scalacheck.*
import Iterablex.*

class IterablexSpec extends Specification with IterableData with ScalaCheckResult:

  "Specification for Iterables extensions".title

  "The sameElementsAs function returns true" >> {
    "if 2 lists of lists contain the same elements in a different order" >> {
      List(List(1), List(2, 3)).sameElementsAs(List(List(3, 2), List(1)))
    }
    "if deeply nested lists have the same elements but in a different order" >> {
      List(1, List(2, 3, List(4)), 5).sameElementsAs(List(5, List(List(4), 2, 3), 1))
    }
    "for 2 iterables created with same elements in a different order" >> {
      given Arbitrary[Iterable[Any]] = arbitraryIterable
      Prop.forAll { (i1: Iterable[Any]) =>
        i1.sameElementsAs(i1.scramble)
      }
    }
    "for 2 iterables created with same elements in a different order, even with different types like LazyList and List" >> {
      given Arbitrary[(Iterable[Any], Iterable[Any])] = sameIterablesOfDifferentTypes
      Prop.forAll { (t: (Iterable[Any], Iterable[Any])) =>
        val (i1, i2) = t
        i1.sameElementsAs(i2)
      }
    }
  }

  "The containsInOrder function should" >> {
    "check that some values are contained inside an Iterable, in the same order" >> {
      List(1, 2, 3).containsInOrder(1, 3)
    }
    "detect if some values are contained inside an Iterable in a different order" >> {
      !List(1, 2, 3).containsInOrder(2, 1)
    }
  }

  "toDeepString uses recursively the toString method to display iterables in brackets" >> {
    List(List(1, 2), 3, List(4, 5)).toDeepString must ===("[[1, 2], 3, [4, 5]]")
  }

  "mapFirst maps the first element with a function if it exists" >> { Seq(1, 2).mapFirst(_ + 1) must ===(Seq(2, 2)) }

  "mapLast maps the last element with a function if it exists" >> { Seq(1, 2).mapLast(_ + 1) must ===(Seq(1, 3)) }

import org.scalacheck.Arbitrary
import org.scalacheck.Gen.*

trait IterableData:

  def arbitraryIterable: Arbitrary[Iterable[Any]] = Arbitrary {
    for
      i0 <- listOfN(3, oneOf(1, 2, 3))
      i1 <- listOfN(3, oneOf[Any](1, 4, 5, i0))
      i2 <- listOfN(3, oneOf[Any](i0, i1, 2, 3))
    yield i2
  }

  val sameIterablesOfDifferentTypes: Arbitrary[(Iterable[Any], Iterable[Any])] = Arbitrary {
    for
      i0 <- listOfN(3, oneOf(1, 2, 3))
      i1 <- listOfN(3, oneOf(1, 2, 3, i0))
    yield (LazyList(i1*), i1.scramble)
  }
