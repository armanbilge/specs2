package org.specs2
package mutable

import matcher.*
import main.ArgumentsShortcuts
import execute.StandardResults
import specification.mutable.*
import specification.create.FormattingFragments
import specification.core.mutable.SpecificationStructure
import specification.dsl.mutable.*

/** Class for a Specification using the mutable DSL and thrown expectations
  */
abstract class Specification extends SpecificationLike

trait SpecificationLike extends SpecificationStructure with SpecificationCreation with SpecificationFeatures

/** Lightweight specification with only 3 implicit methods
  *
  *   - 2 implicits to create the specification string context
  *   - 1 implicit to create expectations with "must"
  *   - 1 implicit to add arguments to the specification
  */
abstract class Spec extends SpecLike

trait SpecLike
    extends SpecificationStructure
    with BlockDsl
    with ArgumentsCreation
    with ArgumentsShortcuts
    with TextCreation
    with ActionDsl
    with MustThrownMatchers
    with Expectations
    with FormattingFragments
    with StandardResults
