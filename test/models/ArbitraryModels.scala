package models

import java.io.File
import java.util.concurrent.TimeUnit

import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.Arbitrary.arbitrary

import scala.concurrent.duration.Duration

import common.io.{IODirectory, IOFile}
import common.rich.path.TempDirectory

object ArbitraryModels {
  private implicit def genToArb[T: Gen]: Arbitrary[T] = Arbitrary(implicitly[Gen[T]])
  // Avoids generating nonsense characters which can mess up JSON parsing
  private val arbitraryString: Gen[String] = arbitrary[String].map(_.filterNot(_.toInt < 60))
  private val arbitraryStringOpt: Gen[Option[String]] =
    Arbitrary.arbOption[String](Arbitrary(arbitraryString)).arbitrary
  implicit lazy val arbSong: Gen[Song] = for {
    filePath <- arbitraryString
    title <- arbitraryString
    artistName <- arbitraryString
    albumName <- arbitraryString
    track <- arbitrary[TrackNumber].map(_ % 100)
    year <- arbitrary[Int].map(_ % 3000)
    bitRate <- arbitrary[Int].map(_ % 10000).map(_ / 32.0).map(_.toString)
    duration <- arbitrary[Int].map(_ % 1000)
    size <- arbitrary[Int]
    discNumber <- arbitraryStringOpt
    trackGain <- arbitrary[Option[Int]].map(_.map(_ % 10000).map(_ / 32.0))
    composer <- arbitraryStringOpt
    conductor <- arbitraryStringOpt
    orchestra <- arbitraryStringOpt
    opus <- arbitraryStringOpt
    performanceYear <- arbitrary[Option[Int]].map(_.map(_ % 3000))
  } yield IOSong(
    IOFile(new File(filePath).getAbsoluteFile),
    title,
    artistName,
    albumName,
    track,
    year,
    bitRate,
    Duration(duration, TimeUnit.SECONDS),
    size,
    discNumber,
    trackGain,
    composer,
    conductor,
    orchestra,
    opus,
    performanceYear,
  )
  implicit lazy val arbAlbumDir: Gen[AlbumDir] = {
    val dir = TempDirectory()
    for {
      title <- arbitraryString
      artistName <- arbitraryString
      year <- arbitrary[Int].map(_ % 3000)
      songs <- arbitrary[Seq[Song]]
    } yield AlbumDir(IODirectory(dir), title, artistName, year, songs)
  }
  implicit lazy val arbArtist: Gen[ArtistDir] = for {
    name <- arbitraryString
    albums <- arbitrary[Set[AlbumDir]]
  } yield ArtistDir(name, albums)
}
