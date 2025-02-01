package backend.recon

import org.scalacheck.{Arbitrary, Gen}
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
  "Album" - {
    "Comparison is based on name" in {
      val a1 = Album("MOoO", 2003, Artist("FooOBAr"))
      val a2 = Album("MoOO", 2003, Artist("foOoBAr"))
      a1 shouldReturn a2
      a1.hashCode shouldReturn a2.hashCode
      a1.toString shouldNot be(a2.toString)
    }
  }
}

object ReconcilablesTest {
  implicit val arbitraryArtist: Arbitrary[Artist] = Arbitrary(Gen.alphaNumStr.map(Artist))
}
