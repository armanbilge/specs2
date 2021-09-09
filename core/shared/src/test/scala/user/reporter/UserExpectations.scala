package user
package specification

import org.specs2.{SpecificationLike, Specification}
import org.specs2.execute.*

class UserExpectations extends Specification:
  def is = s2" $failure1 "

  // used in the MatcherSpec
  def failure1 = ("1" must be_<(0) ^^ ((_: String).toInt)).asInstanceOf[Failure]

class UserExpectationsLike extends SpecificationLike:
  def is = s2" $failure1 "

  // used in the MatcherSpec
  def failure1 = 1 must ===(2)

class UserExpectationsSpec extends org.specs2.mutable.Spec:
  def failure1 = 1 must ===(2)
