package search

import java.io.File

import common.io.{IODirectory, IOFile}
import common.{AuxSpecs, Jsonable}
import models.{Album, Artist, IOSong, Song}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.PropSpec
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import search.ModelJsonable._

class ModelJsonableTest extends PropSpec with GeneratorDrivenPropertyChecks with AuxSpecs
    with Jsonable.ToJsonableOps {
  implicit override val generatorDrivenConfig = PropertyCheckConfig(minSuccessful = 10, workers = 5)
  // Why the fuck is this not in library?!
  private implicit def genToArb[T : Gen]: Arbitrary[T] = Arbitrary(implicitly[Gen[T]])
  private implicit lazy val arbSong: Gen[Song] = for {
    filePath <- arbitrary[String]
    title <- arbitrary[String]
    artistName <- arbitrary[String]
    albumName <- arbitrary[String]
    track <- arbitrary[Int].map(_ % 100)
    year <- arbitrary[Int].map(_ % 3000)
    bitRate <- arbitrary[Int].map(_ % 10000).map(_ / 32.0).map(_.toString)
    duration <- arbitrary[Int].map(_ % 1000)
    size <- arbitrary[Int]
    discNumber <- arbitrary[Option[String]]
    trackGain <- arbitrary[Option[Int]].map(_.map(_ % 10000).map(_ / 32.0))
  } yield {
    IOSong(IOFile(new File(filePath).getAbsoluteFile),
      title, artistName, albumName, track, year, bitRate, duration, size, discNumber, trackGain)
  }
  private implicit lazy val arbAlbum: Gen[Album] = for {
    filePath <- arbitrary[String]
    title <- arbitrary[String]
    artistName <- arbitrary[String]
    year <- arbitrary[Int].map(_ % 3000)
  } yield {
    Album(IODirectory(new File(filePath).getAbsoluteFile), title, artistName, year)
  }
  private implicit lazy val arbArtist: Gen[Artist] = for {
    name <- arbitrary[String]
    albums <- arbitrary[Set[Album]]
  } yield {
    Artist(name, albums)
  }

  private def test[T: Jsonable : Gen](): Unit = {
    forAll { t: T => {
      parseObject[T](t.jsonify).parse shouldReturn t
    }
    }
  }
  property("Songs") { test[Song]() }
  property("Albums") { test[Album]() }
  property("Artists") { test[Artist]() }
}
