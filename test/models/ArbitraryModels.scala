package models

import java.util.concurrent.TimeUnit

import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.Arbitrary.arbitrary

import scala.concurrent.duration.Duration

import common.io.MemoryRoot

object ArbitraryModels {
  private implicit def genToArb[T: Gen]: Arbitrary[T] = Arbitrary(implicitly[Gen[T]])
  // Avoids generating nonsense characters which can mess up JSON parsing
  private val arbitraryString: Gen[String] =
    arbitrary[String].map(_.filterNot(_.toInt < 60).take(5))
  private val arbitraryStringOpt: Gen[Option[String]] =
    Arbitrary.arbOption[String](Arbitrary(arbitraryString)).arbitrary
  private val arbitraryFileName = Gen.nonEmptyStringOf(Gen.alphaNumChar)
  private def arbitraryFile(implicit root: MemoryRoot) =
    arbitraryFileName.filterNot(root.getDir(_).isDefined).map(root.addFile)
  private def arbitraryDirectory(implicit root: MemoryRoot) =
    arbitraryFileName.filterNot(root.getFile(_).isDefined).map(root.addSubDir)
  implicit def arbSong(implicit root: MemoryRoot): Gen[Song] = for {
    file <- arbitraryFile
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
  } yield MemorySong(
    file,
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
  implicit def arbAlbumDir(implicit root: MemoryRoot): Gen[AlbumDir] = for {
    albumDirName <- arbitraryFileName
    dir <- arbitraryDirectory.map(_.addSubDir(albumDirName))
    title <- arbitraryString
    artistName <- arbitraryString
    year <- arbitrary[Int].map(_ % 3000)
    songs <- arbitrary[Seq[Song]]
  } yield AlbumDir(dir, title, artistName, year, songs)
  implicit def arbArtist(implicit root: MemoryRoot): Gen[ArtistDir] = for {
    dir <- arbitraryDirectory
    name <- arbitraryString
    albums <- arbitrary[Set[AlbumDir]]
  } yield ArtistDir(dir, name, albums)
}
