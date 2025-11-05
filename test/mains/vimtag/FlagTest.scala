package mains.vimtag

import common.test.AuxSpecs
import mains.vimtag.Flag.RemoveFeat
import org.scalatest.freespec.AnyFreeSpec

class FlagTest extends AnyFreeSpec with AuxSpecs {
  "RemoveFeat" - {
    "Does nothing to a title with no Feat." in {
      RemoveFeat.removeFeat("Foo Bar") shouldReturn "Foo Bar"
    }
    "Parens" - {
      "Remove Ft." in {
        RemoveFeat.removeFeat("Foo Bar (Ft. Moo)") shouldReturn "Foo Bar"
      }
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
