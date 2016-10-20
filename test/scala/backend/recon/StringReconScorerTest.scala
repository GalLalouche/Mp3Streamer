package backend.recon

import common.AuxSpecs
import org.scalatest.FreeSpec

class StringReconScorerTest extends FreeSpec with AuxSpecs {
  private val $ = StringReconScorer
  def verifyHighReconScore(str1: String, str2: String): Unit = {
    s"$str1 and $str2 should have a high recon score" in {
      $(str1,str2) should be > 0.9
    }
  }
  "ascii" - {
    verifyHighReconScore("Verisäkeet", "Verisakeet")
  }
  "ignores non-letters or digits" - {
    verifyHighReconScore("f,o.o-b=a&r", "foo bar")
    verifyHighReconScore("1984", "1+9_8*4")
  }
  "ignores and" - {
    verifyHighReconScore("Songs of Moors & Misty fields", "Songs of moors and misty fields")
  }
}
