package common.ds
import org.scalatest.{OneInstancePerTest, ShouldMatchers, FreeSpec}

class TrieTest extends FreeSpec with ShouldMatchers with OneInstancePerTest {
  var $ = new Trie[Int]()

  "empty" - {
    "returns an empty for anything" in {
      $ prefixes "foo" should be(empty)
    }
    "has size 0" in {
      $.size should be === 0
    }
  }
  "size" - {
    "1" in {
      $ += "foo" -> 10
      $.size should be === 1
    }
  }
  "apply" - {
    "returns an exact match" in {
      $ += "foo" -> 12
      $ prefixes "foo" should be === List(12)
    }
    "doesn't return a false match" in {
      $ += "foo" -> 12
      $ prefixes "bar" should be (empty)
    }
    "returns all matches with prefix" in {
      $ += "foobar" -> 12
      $ += "foobaz" -> 15
      $ += "spam" -> 10
      $.prefixes("foo").toSet should be === Set(12, 15)
    }
  }
}
