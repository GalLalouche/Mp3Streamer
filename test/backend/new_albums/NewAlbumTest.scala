package backend.new_albums

import java.time.LocalDate

import backend.mb.AlbumType
import backend.recon.{Artist, ReconIDArbitrary}
import backend.recon.ReconcilablesTest.arbitraryArtist
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen

import scalaz.Scalaz.ToApplyOps

import common.JsonableSpecs
import common.rich.RichEnumeratum.richEnumeratum
import common.test.GenInstances.MonadGen
import common.test.ScalaCheckTypes.arbitraryLocalDateBound

class NewAlbumTest extends JsonableSpecs {
  private implicit val ArbitraryNewAlbum: Gen[NewAlbum] =
    (
      Gen.alphaNumStr |@| arbitrary[LocalDate] |@|
        arbitrary[Artist] |@| AlbumType.gen |@| ReconIDArbitrary.gen,
    )(
      new NewAlbum(_, _, _, _, _),
    )

  propJsonTest[NewAlbum]()
}
