package org.specs2

import scala.concurrent.{ Await, Awaitable }
import scala.concurrent.duration.Duration

package object concurrent {

  private[specs2] def awaitResult[A](a: Awaitable[A], d: Duration) = {
    scala.scalanative.runtime.loop()
    Await.result(a, d)
  }

}
