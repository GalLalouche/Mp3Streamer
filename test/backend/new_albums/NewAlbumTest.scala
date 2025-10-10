package backend.new_albums

import backend.mb.AlbumType
import backend.recon.ReconcilablesTest.arbitraryArtist
import backend.recon.{Artist, ReconIDArbitrary}
import cats.implicits.catsSyntaxTuple5Semigroupal
import common.JsonableSpecs
import common.rich.RichEnumeratum.richEnumeratum
import common.test.ScalaCheckTypes.arbitraryLocalDateBound
import common.test.kats.GenInstances.MonadGen
import java.time.LocalDate
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen

class NewAlbumTest extends JsonableSpecs {
  private implicit val ArbitraryNewAlbum: Gen[NewAlbum] =
    (Gen.alphaNumStr, arbitrary[LocalDate], arbitrary[Artist], AlbumType.gen, ReconIDArbitrary.gen)
      .mapN(new NewAlbum(_, _, _, _, _))

  propJsonTest[NewAlbum]()
}
