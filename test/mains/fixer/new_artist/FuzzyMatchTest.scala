package mains.fixer.new_artist

import org.scalatest.FreeSpec

import common.test.AuxSpecs

class FuzzyMatchTest extends FreeSpec with AuxSpecs {
  "non-empty with empty string is no match" in {
    FuzzyMatch("foo", "") shouldReturn false
  }
  "empty with non-empty string is match" in {
    FuzzyMatch("", "foo") shouldReturn true
  }
  "empty with empty string is no match" in {
    FuzzyMatch("", "") shouldReturn true
  }
  "exact string is match" in {
    FuzzyMatch("foo", "foo") shouldReturn true
  }
  "no fuzz is no match" in {
    FuzzyMatch("foo", "bar") shouldReturn false
  }
  "match on prefix" in {
    FuzzyMatch("foo", "foobar") shouldReturn true
  }
  "match on suffix" in {
    FuzzyMatch("foo", "barfoo") shouldReturn true
  }
  "match on fuzzy" in {
    FuzzyMatch("foo", "f1o2o3") shouldReturn true
  }
  "Smart casing" - {
    "No match due to upper case in source" in {
      FuzzyMatch("Foo", "f1o2o3") shouldReturn false
    }
    "match due to ignoring case in dest when there is no casing in source" in {
      FuzzyMatch("foo", "F1O2O3") shouldReturn true
    }
    "Matching with casing" in {
      FuzzyMatch("fOo", "fo1O2o3") shouldReturn true
    }
  }
}
