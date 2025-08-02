package musicfinder

import models.ArbitraryModels
import org.scalacheck.Arbitrary

import common.JsonableSpecs

private class ArtistToDirectoryTest extends JsonableSpecs {
  implicit def arbitraryArtistToDirectory: Arbitrary[ArtistToDirectory] =
    Arbitrary(ArbitraryModels.arbArtist.map(ArtistToDirectory.from))

  propJsonTest[ArtistToDirectory]()
}
