package common.ds

import org.scalatest.{FreeSpec, Matchers, OneInstancePerTest}

import common.test.AuxSpecs

class TrieTest extends FreeSpec with OneInstancePerTest with Matchers with AuxSpecs {
  private var $ = Trie.empty[Int]

  "empty" - {
    "returns an empty for anything" in {
      $.withPrefix("foo") shouldBe empty
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

  "withPrefix" - {
    "returns an exact match" in {
      $ += "foo" -> 12
      $ withPrefix "foo" shouldReturn Vector(12)
    }
    "doesn't return a false match" in {
      $ += "foo" -> 12
      $ withPrefix "bar" shouldBe empty
    }
    "returns all matches with prefix" in {
      $ += "foobar" -> 12
      $ += "foobar" -> 42
      $ += "foobaz" -> 15
      $ += "spam" -> 10
      $ should have size 4
      $.withPrefix("foo") shouldMultiSetEqual Vector(12, 42, 15)
    }
  }

  "exact" - {
    "returns an exact match" in {
      $ += "foo" -> 12
      $ withPrefix "foo" shouldReturn Vector(12)
    }
    "doesn't return a false match" in {
      $ += "foo" -> 12
      $ withPrefix "bar" shouldBe empty
    }
    "returns nothing if no exact match" in {
      $ += "foobar" -> 12
      $ += "foobaz" -> 15
      $ += "spam" -> 10
      $ should have size 3
      $.exact("foo") shouldBe empty
    }
    "returns exact matches only" in {
      $ += "foobar" -> 12
      $ += "foobaz" -> 15
      $ += "spam" -> 10
      $ += "foo" -> 1
      $ += "foo" -> 2
      $ should have size 5
      $.exact("foo") shouldMultiSetEqual Vector(1, 2)
    }
  }

  "from" - {
    "fromMap" in {
      val $ = Trie.fromMap(Map("foo" -> 1, "bar" -> 2, "foobar" -> 3))
      $ should have size 3
      $.exact("foo") shouldReturn Vector(1)
      $.withPrefix("foo") shouldMultiSetEqual Vector(1, 3)
    }
    "fromMultiMap" in {
      val $ = Trie.fromMultiMap(Map("foo" -> Vector(1, 4), "bar" -> Vector(2), "foobar" -> Vector(3)))
      $ should have size 4
      $.exact("foo") shouldMultiSetEqual Vector(1, 4)
      $.withPrefix("foo") shouldMultiSetEqual Vector(1, 4, 3)
    }
  }
}
