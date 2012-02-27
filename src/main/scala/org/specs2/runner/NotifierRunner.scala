package org.specs2
package runner

import reporter._
import main.Arguments
import specification.{ExecutedSpecification, ExecutingSpecification}

/**
 * This runner can be used with any class implementing the Notifier trait
 */
case class NotifierRunner(notifier: Notifier) { outer =>

  def main(arguments: Array[String]) = new ClassRunner {
    override lazy val reporter: Reporter = new NotifierReporter {
      val notifier = outer.notifier
      override def export(implicit arguments: Arguments): ExecutingSpecification => ExecutedSpecification = (spec: ExecutingSpecification) => {
        super.export(arguments)(spec)
        exportToOthers(arguments)(spec)
        spec.executed
      }

    }
  }.main(Array(arguments:_*))

}