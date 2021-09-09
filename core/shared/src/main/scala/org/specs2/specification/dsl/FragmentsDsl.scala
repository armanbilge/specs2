package org.specs2
package specification
package dsl

import core.*
import create.*
import org.specs2.collection.Vectorx.*

/** Creation of Fragments with the ^ syntax
  */
trait FragmentsDsl extends FragmentsFactory with AcceptanceDsl1:

  trait ToFragments[T]:
    def toFragments(t: T): Fragments

  given ToFragments[Fragment] with
    def toFragments(f: Fragment): Fragments =
      f

  given ToFragments[Fragments] with
    def toFragments(fs: Fragments): Fragments =
      fs

  given ToFragments[String] with
    def toFragments(s: String): Fragments =
      fragmentFactory.text(s)

  given ToFragments[Seq[Fragment]] with
    def toFragments(fs: Seq[Fragment]): Fragments =
      Fragments(fs*)

  implicit class appendFragments[T1: ToFragments, T2: ToFragments](t1: T1):
    def ^(t2: T2): Fragments =
      summon[ToFragments[T1]].toFragments(t1).append(summon[ToFragments[T2]].toFragments(t2))

  extension (fragment: Fragment)
    def hide: Fragment =
      fragment.description match
        case r: SpecificationRef => fragment.copy(description = r.hide)
        case other               => fragment.copy(description = NoText)

  extension (fragment: Fragment)
    def mute: Fragment =
      fragment.description match
        case r: SpecificationRef => fragment.copy(description = r.mute)
        case other               => fragment.copy(description = NoText)

  /** create a block of new fragments where each of them is separated by a newline and there is a specific offset from
    * the left margin
    */
  def fragmentsBlock(fragments: Seq[Fragment], offset: Int = 2): Fragments =
    val newLine = Vector(fragmentFactory.break, fragmentFactory.text(" " * offset))
    (newLine ++ fragments.toList)
      .map(Fragments(_))
      .intersperse(Fragments(newLine*))
      .reduce(_ `append` _)

object FragmentsDsl extends FragmentsDsl
