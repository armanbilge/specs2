package org.specs2
package guide

import BuildInfo.*

object Specs2Variables extends Specs2Variables

trait Specs2Variables:

  lazy val VERSION = s"SPECS2-${BuildInfo.version}"
  lazy val GUIDE_DIR = s"https://etorreborre.github.io/specs2/guide/$VERSION"
  lazy val SITE_DIR = s"https://etorreborre.github.io/specs2/website/$VERSION"
  lazy val API_DIR = s"https://etorreborre.github.io/specs2/api/$VERSION/api"
  lazy val PROJECT_DIR = s"https://github.com/etorreborre/specs2/tree/$VERSION"
  lazy val EXAMPLES_DIR = s"$PROJECT_DIR/examples/shared/src/test/scala/examples"

  lazy val specs2Variables: Map[String, String] =
    Map(
      "VERSION" -> VERSION,
      "SITE_DIR" -> SITE_DIR,
      "PROJECT_DIR" -> PROJECT_DIR,
      "GUIDE_DIR" -> GUIDE_DIR,
      "EXAMPLES_DIR" -> EXAMPLES_DIR,
      "API_DIR" -> API_DIR
    )
