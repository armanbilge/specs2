package org.specs2
package execute

import control.*

/** This class is used to delay the execution of a result
  */
class Function0Result(var t: () => Result):
  def :=[R: AsResult](r: =>R) =
    t = () => AsResult(r)
    this

object Function0Result:

  given anyResultAsResult[T]: AsResult[Function0Result] with
    def asResult(code: =>Function0Result): Result =
      code.t()

  implicit def toFunction0Result[T: AsResult](t: =>T): Function0Result =
    new Function0Result(() => AsResult(t))
