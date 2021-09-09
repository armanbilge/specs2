package org.specs2
package specification

import io.*
import fp.syntax.*
import execute.AsResult
import control.*
import process.*
import FileName.*

class StoreSpec extends Specification {
  def is = sequential ^ s2"""
 The file store stores values in files where the name of the file is
   defined by the key $e1

 The store can be reset $e2

"""

  def e1 =
    val store: DirectoryStore = DirectoryStore("target" / "test", FileSystem(ConsoleLogger()))
    val key = SpecificationStatsKey("name")
    (store.set(key, Stats(1)) >> store.get(key)).map(_ must beSome(Stats(1)))

  def e2 =
    val store: DirectoryStore = DirectoryStore("target" / "test", FileSystem(ConsoleLogger()))
    AsResult(e1)

    val key = SpecificationStatsKey("name")
    (store.reset >> store.get(key)).map(_ must beNone)

}
