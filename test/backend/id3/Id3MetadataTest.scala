package backend.id3

import common.JsonableSpecs
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.PropSpec
import org.scalatest.prop.GeneratorDrivenPropertyChecks

class Id3MetadataTest extends PropSpec with JsonableSpecs with GeneratorDrivenPropertyChecks {
  private implicit val arbitraryId3Metadata: Arbitrary[Id3Metadata] = Arbitrary(for {
    title <- arbitrary[String]
    artistName <- arbitrary[String]
    albumName <- arbitrary[String]
    track <- arbitrary[Int]
    year <- arbitrary[Int]
    discNumber <- arbitrary[String]
  } yield Id3Metadata(title = title, artistName = artistName, albumName = albumName, track = track, year = year, discNumber = discNumber))

  propJsonTest[Id3Metadata]()
}
