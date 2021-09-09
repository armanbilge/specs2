package org.specs2
package mutable

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class FutureExpectationsImmutableSpec extends org.specs2.Specification {
  def is = s2"""

 A specification can return future results
   For example here $example1

"""

  def example1 =
    Future.apply(1 === 1)

}

class FutureExpectationsMutableSpec extends org.specs2.mutable.Spec:

  "A specification can return future results" >> {
    "For example here" >> {
      Future.apply(1 === 1)
    }
    "For example there" >> { (withTitle: String) =>
      Future.apply(withTitle === "For example there")
    }

  }
