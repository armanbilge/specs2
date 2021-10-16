package examples

import org.specs2.*

class HelloWorldUnitSpec extends mutable.Specification:
  "HW" >> {
    "The 'Hello world' string" should {
      "contain 11 characters" in {
        "Hello world" must haveSize(11)
      }
      "start with 'Hello'" in {
        "Hello world" must startWith("Hello")
      }
      "end with 'world'" in {
        "Hello world" must endWith("world")
      }
    }
  }
