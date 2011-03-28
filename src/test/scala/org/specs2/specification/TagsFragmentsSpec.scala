package org.specs2
package specification
import matcher.DataTables
import mutable.SpecificationWithJUnit
import TagsFragments._

class TagsFragmentsSpec extends SpecificationWithJUnit with DataTables {
  val tag = TaggedAs("t")

  "A tagging fragment containing the tag 't' will keep fragments depending on the include/exclude arguments" >> {
    "include"    || "exclude" | "keep" |>
    ""           !! ""        ! true   |
    "t"          !! ""        ! true   |
    ""           !! "t"       ! false  |
    "t2"         !! ""        ! false  |
    "t"          !! "t2"      ! true   |
    "t2"         !! "t"       ! false  |
    "t,t2"       !! ""        ! true   |
    ""           !! "t,t2"    ! false  |
    ""           !! "t, t2"   ! false  | // adding some whitespace noise
    ""           !! " t, ,t2" ! false  | // adding some whitespace noise
    "t,t2"       !! "t3"      ! true   |
    "t2"         !! "t,t3"    ! false  | { (inc, exc, keep) =>
      tag.keep(args(include=inc, exclude=exc)) must be_==(keep)
    }
  }
}