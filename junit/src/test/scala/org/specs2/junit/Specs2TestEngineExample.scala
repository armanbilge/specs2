package org.specs2
package junit

import org.specs2.specification.create.DefaultFragmentFactory

class Specs2TestEngineMutableExample extends mutable.Specification:
  step(println("executing first step"))

  "this is a block" >> {
    "test-1" >> ok
    "test-2" >> ko
  }

  "this is another block" >> {
    "test-1" >> ok
    "test-2" >> ko
  }

  step(println("executing last step"))

class Specs2TestEngineExample extends Specification:
  def is = s2"""
     ${step(println("executing first step"))}

     this is a block
       test-1 $ok
       test-2 $ko

     this is another block
       test-1 $ok
       test-2 ${anError("boom")}

     ${step(println("executing last step"))}

  """
