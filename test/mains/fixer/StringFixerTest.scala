package mains.fixer

import common.AuxSpecs
import org.scalatest.FreeSpec

class StringFixerTest extends FreeSpec with AuxSpecs {
  private val $ = StringFixer

  private def verifyFix(original: String, fix: String): Unit = {
    $(original) shouldReturn fix
  }

  "Ordered numerals" in {
    verifyFix("1st", "1st")
    verifyFix("2nd", "2nd")
    verifyFix("3rd", "3rd")
    verifyFix("4th", "4th")
    verifyFix("27th", "27th")
    verifyFix("101st", "101st")
  }
  "I and a" in {
    verifyFix("a foo", "A Foo")
    verifyFix("foo a", "Foo a")
    verifyFix("i foo", "I Foo")
    verifyFix("foo i", "Foo I")
  }
  "Mixed caps" in {
    List("FOO", "FoO", "fOo").foreach(e => verifyFix(e, e))
  }
  "Capitalize" in {
    List("foobar", "Foobar").foreach(verifyFix(_, "Foobar"))
  }
  "Sentence" in {
    verifyFix("i am a rock", "I am a Rock")
    verifyFix("a rock am i", "A Rock am I")
  }
  "Is sorted" in {
    $.lowerCaseWords.sorted shouldReturn $.lowerCaseWords
  }
  "With dots" in {
    List("F.F.S.", "f.f.s.").foreach(verifyFix(_, "F.F.S."))
  }
  "Roman numerals" in {
    verifyFix("mmxvi", "MMXVI")
  }
  "Asciify" in {
    verifyFix("Köyliönjärven jäällä", "Koylionjarven Jaalla")
    verifyFix("Епосі нескорених поетів", "Eposi Neskorenikh Poetiv")
  }
  "with delimiters" in {
    verifyFix("foo/bar", "Foo/Bar")
  }
  "strips" in {
    verifyFix(" Foo Bar ", "Foo Bar")
  }
  "stupid apostrophe" in {
    verifyFix("I’m a stupid apostrophe", "I'm a Stupid Apostrophe")
  }
}
