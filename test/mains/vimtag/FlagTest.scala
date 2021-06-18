package mains.vimtag

import mains.vimtag.Flag.RemoveFeat
import org.scalatest.FreeSpec

import common.test.AuxSpecs

class FlagTest extends FreeSpec with AuxSpecs {
  "RemoveFeat" - {
    "Does nothing to a title with no Feat." in {
      RemoveFeat.removeFeat("Foo Bar") shouldReturn "Foo Bar"
    }
    "Parens" - {
      "Remove Feat." in {
        RemoveFeat.removeFeat("Foo Bar (Feat. Moo)") shouldReturn "Foo Bar"
      }
      "Remove Featuring" in {
        RemoveFeat.removeFeat("Foo Bar (Featuring Moo)") shouldReturn "Foo Bar"
      }
      "Case insensitive Feat." in {
        RemoveFeat.removeFeat("Foo Bar (fEAt. Moo)") shouldReturn "Foo Bar"
      }
      "Case insensitive Featuring" in {
        RemoveFeat.removeFeat("Foo Bar (fEATurIng Moo)") shouldReturn "Foo Bar"
      }
    }
    "No parens" - {
      "Remove Feat." in {
        RemoveFeat.removeFeat("Foo Bar Feat. Moo") shouldReturn "Foo Bar"
      }
    }
  }
}
