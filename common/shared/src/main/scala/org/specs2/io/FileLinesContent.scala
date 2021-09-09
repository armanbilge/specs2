package org.specs2
package io

import java.io.File
import text.*

/** Default implementation for reading lines out of a file
  *
  * This is used in FileContentMatchers
  */
object FileLinesContent extends LinesContent[File]:
  def lines(f: File): Seq[String] =
    if f.isDirectory then Seq()
    else FilePathReader.readLines(FilePath.unsafe(f)).runOption.getOrElse(Seq())

  def name(f: File) = f.getPath
