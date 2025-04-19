package backend.recon

import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.FreeSpec

import scalaz.Scalaz.ToApplyOps

import common.test.AuxSpecs
import common.test.GenInstances.MonadGen

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
  private val nonEmptyString = Gen.nonEmptyStringOf(Gen.alphaNumChar)
  implicit val arbitraryArtist: Arbitrary[Artist] =
    Arbitrary(nonEmptyString.map(Artist.apply))
  implicit val arbitraryAlbum: Arbitrary[Album] =
    Arbitrary((nonEmptyString |@| Gen.choose(1500, 2500) |@| arbitrary[Artist])(Album.apply))
  implicit val arbitraryTrack: Arbitrary[Track] =
    Arbitrary((nonEmptyString |@| arbitrary[Album])(Track.apply))
}
