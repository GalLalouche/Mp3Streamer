package common.ds

import common.AuxSpecs
import org.scalatest.{FreeSpec, Matchers, OneInstancePerTest}

class TrieTest extends FreeSpec with OneInstancePerTest with Matchers with AuxSpecs {
  private var $ = new Trie[Int]()

  "empty" - {
    "returns an empty for anything" in {
      $.prefixes("foo") shouldBe empty
    }
    "has size 0" in {
      $ should have size 0
    }
  }
  "size" - {
    "1" in {
      $ += "foo" -> 10
      $ should have size 1
    }
  }
  "apply" - {
    "returns an exact match" in {
      $ += "foo" -> 12
      $ prefixes "foo" shouldReturn List(12)
    }
    "doesn't return a false match" in {
      $ += "foo" -> 12
      $ prefixes "bar" shouldBe empty
    }
    "returns all matches with prefix" in {
      $ += "foobar" -> 12
      $ += "foobaz" -> 15
      $ += "spam" -> 10
      $.prefixes("foo").toSet shouldReturn Set(12, 15)
    }
  }
}
