package backend.recon

import backend.module.TestModuleConfiguration
import common.test.AuxSpecs
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import org.scalatest.freespec.AnyFreeSpec

class StringReconScorerTest extends AnyFreeSpec with AuxSpecs {
  private val $ = TestModuleConfiguration().injector.instance[StringReconScorer]

  def verifyLowReconScore(str1: String, str2: String): Unit =
    s"<$str1> and <$str2> should have a low recon score" in {
      $(str1, str2) should be <= 0.9
    }
  def verifyHighReconScore(str1: String, str2: String): Unit =
    s"<$str1> and <$str2> should have a high recon score" in {
      $(str1, str2) should be > 0.9
    }

  "An empty string has a low recon score" - {
    verifyLowReconScore("", "   ")
  }
  "ascii is normalized" - {
    verifyHighReconScore("Verisäkeet", "Verisakeet")
    verifyHighReconScore("Mære", "Maere")
  }
  "ignores non-letters or digits" - {
    verifyHighReconScore("f,o.o-b=a&r", "foo bar")
    verifyHighReconScore("1984", "1+9_8*4")
  }
  "ignores and" - {
    verifyHighReconScore("Songs of Moors & Misty fields", "Songs of moors and misty fields")
  }
  "ignores punctuations" - {
    verifyHighReconScore("Grand Opening and Closing!", "Grand Opening and Closing")
  }
  "Hebrew" - {
    "Exact match returns true" - {
      verifyHighReconScore("משה", "משה")
    }
    "Non-exact else returns false" - {
      verifyLowReconScore("חיים", "משה")
    }
    "English and hebrew returns true" - {
      verifyHighReconScore("Habiluim - הבילויים", "הבילויים")
      verifyHighReconScore("Sheger Peger - שגר פגר", "שגר פגר")
    }
  }
  "Correctly tokenizes" - {
    verifyHighReconScore("... and Again into the Light", "…And Again Into the Light")
  }
}
