package org.specs2
package specification
package process

import core.*
import fp.syntax.*
import Arbitraries.{given}
import Fragment.*
import control.*
import producer.*, Producer.*
import concurrent.ExecutionEnv

class IndentationSpec(using ee: ExecutionEnv) extends Specification with ScalaCheck {
  def is = s2"""

 The Indentation fold is responsible for computing the indentation level in a specification
 based on the presence of tab fragments

 At all times the indentation number
  must never be negative                            $positive
  must be less than or equal to the number of tabs  $lessThanOrEqualTabs
  must be exactly the sum of tabs if no backtabs    $equalTabsWhenNoBacktabs
"""

  def positive = prop { (fs: Fragments) =>
    indentation(fs) must beSome(be_>=(0))
  }

  def lessThanOrEqualTabs = prop { (fs: Fragments) =>
    val tabsNumber = fs.fragmentsList(ee).collect { case Fragment(Tab(n), _, _) => n }.sumAll
    indentation(fs) must beSome(be_<=(tabsNumber))
  }

  def equalTabsWhenNoBacktabs = prop { (fs: Fragments) =>
    val tabsNumber = fs.fragmentsList(ee).collect { case Fragment(Tab(n), _, _) => n }.sumAll
    indentation(fs.filter(!isBacktab(_))) must beSome(tabsNumber)
  }

  def indentation(fs: Fragments): Option[Int] =
    fs.contents.fold(Indentation.fold.into[Action]).runOption(ee)
}
