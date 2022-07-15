package models

import java.io.File
import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.Arbitrary.arbitrary

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration

import common.io.{IODirectory, IOFile}

object ArbitraryModels {
  private implicit def genToArb[T: Gen]: Arbitrary[T] = Arbitrary(implicitly[Gen[T]])
  implicit lazy val arbSong: Gen[Song] = for {
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
    composer <- arbitrary[Option[String]]
    conductor <- arbitrary[Option[String]]
    orchestra <- arbitrary[Option[String]]
    opus <- arbitrary[Option[String]]
    performanceYear <- arbitrary[Option[Int]].map(_.map(_ % 3000))
  } yield {
    IOSong(IOFile(new File(filePath).getAbsoluteFile),
      title, artistName, albumName, track, year, bitRate,
      Duration(duration, TimeUnit.SECONDS), size, discNumber, trackGain,
      composer, conductor, orchestra, opus, performanceYear,
    )
  }
  implicit lazy val arbAlbum: Gen[Album] = for {
    filePath <- arbitrary[String]
    title <- arbitrary[String]
    artistName <- arbitrary[String]
    year <- arbitrary[Int].map(_ % 3000)
    songs <- arbitrary[Seq[Song]]
  } yield {
    Album(IODirectory(new File(filePath).getAbsoluteFile), title, artistName, year, songs)
  }
  implicit lazy val arbArtist: Gen[Artist] = for {
    name <- arbitrary[String]
    albums <- arbitrary[Set[Album]]
  } yield {
    Artist(name, albums)
  }
}
