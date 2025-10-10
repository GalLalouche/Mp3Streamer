package backend.recon

import cats.implicits.{catsSyntaxTuple2Semigroupal, catsSyntaxTuple3Semigroupal}
import common.test.AuxSpecs
import common.test.MoreGen.nonEmptyAlphaNumString
import common.test.kats.GenInstances.MonadGen
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen.choose
import org.scalatest.FreeSpec

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
  implicit val arbitraryArtist: Arbitrary[Artist] =
    Arbitrary(nonEmptyAlphaNumString.map(Artist.apply))
  implicit val arbitraryAlbum: Arbitrary[Album] =
    Arbitrary((nonEmptyAlphaNumString, choose(1000, 9999), arbitrary[Artist]).mapN(Album.apply))
  implicit val arbitraryTrack: Arbitrary[Track] =
    Arbitrary((nonEmptyAlphaNumString, arbitrary[Album]).mapN(Track.apply))
}
