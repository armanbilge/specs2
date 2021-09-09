package org.specs2
package collection

import scala.annotation.tailrec

/** This trait provides additional methods on Lists and nested Lists
  */
private[specs2] trait Listx:

  /** Additional methods for nested lists
    */
  extension [T](list: List[List[T]])
    def safeTranspose: List[List[T]] =
      transpose(list)

  /** Additional methods for lists
    */
  extension [T](list: List[T])
    /** @return
      *   a randomly mixed list
      */
    def scramble: List[T] =
      list.sortWith((a, b) => (new java.util.Random).nextInt(2) > 0)

    def intersperse(a: T): List[T] =
      @tailrec
      def intersperse0(accum: List[T], rest: List[T]): List[T] = rest match
        case List()  => accum
        case List(x) => x :: accum
        case h :: t  => intersperse0(a :: h :: accum, t)
      intersperse0(Nil, list).reverse

  /** This methods works like the transpose method defined on Traversable but it doesn't fail when the input is not
    * formatted like a regular matrix
    *
    * List(List("a", "bb", "ccc"), List("dd", "e", "fff")) => List(List("a", "dd"), List("e", "bb") List("ccc", "fff"))
    */
  def transpose[T](xs: List[List[T]]): List[List[T]] =
    val filtered = xs.filter(_.nonEmpty)
    if filtered.isEmpty then Nil
    else filtered.map(_.head) :: transpose(filtered.map(_.tail))

private[specs2] object Listx extends Listx
