package mains.fixer.new_artist

import common.rich.primitives.RichBoolean.richBoolean
import common.test.AuxSpecs
import org.scalatest.FreeSpec

class FuzzyMatchTest extends FreeSpec with AuxSpecs {
  private implicit class MatchedString($ : String) {
    def fuzzyMatches(dest: String) = assert(FuzzyMatch($, dest), "Should have fuzzy matched")
    def doesNotFuzzyMatch(dest: String) =
      assert(FuzzyMatch($, dest).isFalse, "Should not have fuzzy matched")
  }
  "non-empty with empty string is no match" in {
    "foo".doesNotFuzzyMatch("")
  }
  "empty with non-empty string is match" in {
    "".fuzzyMatches("foo")
  }
  "empty with empty string is a match" in {
    "".fuzzyMatches("")
  }
  "exact string is match" in {
    "foo".fuzzyMatches("foo")
  }
  "no fuzz is no match" in {
    "foo".doesNotFuzzyMatch("bar")
  }
  "match on prefix" in {
    "foo".fuzzyMatches("foobar")
  }
  "match on suffix" in {
    "foo".fuzzyMatches("barfoo")
  }
  "match on fuzzy" in {
    "foo".fuzzyMatches("f1o2o3")
  }
  "Other options" - {
    "Smart casing" - {
      "No match due to upper case in source" in {
        "Foo".doesNotFuzzyMatch("f1o2o3")
      }
      "match due to ignoring case in dest when there is no casing in source" in {
        "foo".fuzzyMatches("F1O2O3")
      }
      "Matching with casing" in {
        "fOo".fuzzyMatches("fo1O2o3")
      }
    }
    "Starting ^" - {
      "Will match if the prefixes match" in {
        "^fbr".fuzzyMatches("foobar")
      }
      "Will not match if the prefixes don't match" in {
        "^oobr".doesNotFuzzyMatch("foobar")
      }
      "Is not case sensitive by default" in {
        "^fbr".fuzzyMatches("FOobaR")
      }
    }
    "Literal '" - {
      "Will match if the whole word matches" in {
        "'foo bar".fuzzyMatches("foo bzarzazr")
      }
      "Will not match if the whole word does not matches" in {
        "'foo bar".doesNotFuzzyMatch("foobzarzazr")
      }
      "Can use ' in the middle of the word" in {
        "moo 'foo bar".doesNotFuzzyMatch("maasd8ejasdaoasd8asdjofooasdfjasdfbaasdfjr")
      }
    }
  }
}
