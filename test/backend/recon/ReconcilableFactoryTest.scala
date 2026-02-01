package backend.recon

import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.freespec.AnyFreeSpec

import common.test.AuxSpecs

class ReconcilableFactoryTest extends AnyFreeSpec with AuxSpecs {
  "Parsing song info from file path" - {
    "dash syntax " in {
      ReconcilableFactory.capture("01 - foo bar.mp3").value shouldReturn (1, "foo bar")
    }
    "dot syntax" in {
      ReconcilableFactory.capture("01. foo bar.flac").value shouldReturn (1, "foo bar")
    }
  }
}
