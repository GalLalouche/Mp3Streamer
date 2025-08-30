package models

import java.io.File
import java.util.concurrent.TimeUnit

import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.OneInstancePerTest
import org.scalatest.tags.Slow

import scala.concurrent.duration.Duration

import common.JsonableSpecs
import common.io.{IODirectory, IOFile, IOPathRefFactory}
import common.rich.path.TempDirectory

@Slow
class IOModelJsonableTest extends JsonableSpecs with OneInstancePerTest {
  import IOModelJsonableTest._
  private val mj = new ModelJsonable(ModelJsonable.IOSongJsonParser, IOPathRefFactory)
  import mj._
  propJsonTest[Song]()
  propJsonTest[AlbumDir]()
  propJsonTest[ArtistDir]()
}

private object IOModelJsonableTest {
  // Avoids generating nonsense characters which can mess up JSON parsing
  private val arbitraryString: Gen[String] = arbitrary[String].map(_.filterNot(_.toInt < 60))
  private val arbitraryStringOpt: Gen[Option[String]] =
    Arbitrary.arbOption[String](Arbitrary(arbitraryString)).arbitrary
  private implicit lazy val arbSong: Gen[Song] = for {
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
  private implicit def genToArb[T: Gen]: Arbitrary[T] = Arbitrary(implicitly[Gen[T]])
  private implicit lazy val arbAlbumDir: Gen[AlbumDir] = {
    val dir = TempDirectory()
    for {
      title <- arbitraryString
      artistName <- arbitraryString
      year <- arbitrary[Int].map(_ % 3000)
      songs <- arbitrary[Seq[Song]]
    } yield AlbumDir(IODirectory(dir), title, artistName, year, songs)
  }
  private implicit lazy val arbArtist: Gen[ArtistDir] = {
    val dir = TempDirectory()
    for {
      name <- arbitraryString
      albums <- arbitrary[Set[AlbumDir]]
    } yield ArtistDir(IODirectory(dir), name, albums)
  }
}
