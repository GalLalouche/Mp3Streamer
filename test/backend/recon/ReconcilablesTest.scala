package backend.recon

import org.scalatest.FreeSpec

import common.test.AuxSpecs

class ReconcilablesTest extends FreeSpec with AuxSpecs {
  "Artist" - {
    "Comparison is based on name" in {
      val a1 = Artist("FooOBAr")
      val a2 = Artist("fOOoBar")
      a1 shouldReturn a2
      a1.hashCode shouldReturn a2.hashCode
      a1.toString shouldNot be(a2.toString)
    }
  }
}
