package backend.recon

import common.test.AuxSpecs
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.OptionValues.convertOptionToValuable

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
