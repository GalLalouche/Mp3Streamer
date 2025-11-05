package backend.recon

import common.test.AuxSpecs
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.OptionValues._

class ReconIDTest extends AnyFreeSpec with AuxSpecs {
  "Invalid" - {
    "nonsense" in {
      ReconID.validate("foobar") shouldReturn None
    }
    "incorrect dashes" in {
      ReconID.validate("0383dadf-2a4e4d10-a46a-e9e041da8eb3") shouldReturn None
    }
  }
  "valid" - {
    "plain" in {
      ReconID.validate("0383dadf-2a4e-4d10-a46a-e9e041da8eb3").value shouldReturn
        ReconID("0383dadf-2a4e-4d10-a46a-e9e041da8eb3")
    }
    "With http" in {
      ReconID
        .validate("https://musicbrainz.org/artist/70248960-cb53-4ea4-943a-edb18f7d336f")
        .value shouldReturn ReconID("70248960-cb53-4ea4-943a-edb18f7d336f")
    }
  }
}
