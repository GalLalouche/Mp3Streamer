package backend.recon

import org.scalatest.FreeSpec

import org.scalatest.OptionValues._
import common.test.AuxSpecs

class ReconIDTest extends FreeSpec with AuxSpecs {
  "Invalid" - {
    "nonsense" in {
      ReconID.validate("foobar") shouldReturn None
    }
    "incorrect dashes" in {
      ReconID.validate("0383dadf-2a4e4d10-a46a-e9e041da8eb3") shouldReturn None
    }
  }
  "valid" in {
    ReconID.validate("0383dadf-2a4e-4d10-a46a-e9e041da8eb3").value shouldReturn
        ReconID("0383dadf-2a4e-4d10-a46a-e9e041da8eb3")
  }
}
