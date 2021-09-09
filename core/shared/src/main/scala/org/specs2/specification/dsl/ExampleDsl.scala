package org.specs2
package specification
package dsl

import specification.create.FragmentsFactory
import execute.AsResult
import org.specs2.specification.core.{Text, Execution, Env, Fragment}
import control.ImplicitParameters.ImplicitParam
import scala.util.NotGiven

/** Example Dsl for mutable specifications
  */
trait ExampleDsl extends FragmentsFactory:

  extension (d: String)(using not: NotGiven[NoBangExamples])
    def !(execution: =>Execution): Fragment =
      fragmentFactory.example(Text(d), execution)

  extension [R: AsResult](d: String)(using not: NotGiven[NoBangExamples])
    def !(r: =>R): Fragment =
      fragmentFactory.example(d, r)

    def !(r: String => R): Fragment =
      fragmentFactory.example(d, r)

  extension [R: AsResult](d: String)(using not: NotGiven[NoBangExamples])
    def !(r: Env => R)(using p: ImplicitParam): Fragment =
      fragmentFactory.example(d, r)

  def addExample(d: String, execution: =>Execution): Fragment =
    fragmentFactory.example(Text(d), execution)

trait NoBangExamples:
  given NoBangExamples = ???
