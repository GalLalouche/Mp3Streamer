package mains.fixer

import common.AuxSpecs
import org.scalatest.FreeSpec

class StringFixerTest extends FreeSpec with AuxSpecs {
  private val $ = StringFixer
  private def verifyFix(original: String, fix: String) {
    s"$original should equal $fix" in {
      $(original) shouldReturn fix
    }
  }

  "one word" - {
    verifyFix("1St", "1st")
    verifyFix("2Nd", "2nd")
    verifyFix("3Rd", "3rd")
    verifyFix("4th", "4th")
  }
  "capitalize" - {
    List("foobar", "FOOBAR", "Foobar", "FoOBaR", "fOObAr").foreach(verifyFix(_, "Foobar"))
  }
  "sentence" - {
    verifyFix("I Am A Rock", "I am a Rock")
  }
  "ordered" in {
    $.lowerCaseWords.sorted shouldReturn $.lowerCaseWords
  }
  "asciify" - {
    verifyFix("Köyliönjärven jäällä", "Koylionjarven Jaalla")
  }
}
