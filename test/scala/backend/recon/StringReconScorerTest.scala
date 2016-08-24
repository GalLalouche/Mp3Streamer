package backend.recon

import common.AuxSpecs
import org.scalatest.FreeSpec

class StringReconScorerTest extends FreeSpec with AuxSpecs {
  val $ = StringReconScorer
  "ascii" in {
    $("Verisäkeet", "Verisakeet") should be > 0.9
  }
}
