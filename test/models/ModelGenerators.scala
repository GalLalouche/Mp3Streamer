package models

import java.io.File
import java.util.concurrent.TimeUnit

import org.scalacheck.{Arbitrary, Gen, Shrink}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.util.Buildable

import scala.concurrent.duration.Duration

import common.path.ref.io.TempDirectory

private object ModelGenerators {
  private val tempDir = TempDirectory()
  private val arbitraryString: Gen[String] = for {
    c <- Gen.alphaNumChar
    s <- Gen.alphaNumStr.map(_.filterNot(_.toInt < 60))
  } yield c + s
  private val arbitraryStringOpt: Gen[Option[String]] =
    Arbitrary.arbOption[String](Arbitrary(arbitraryString)).arbitrary
  private val arbitraryFileName: Gen[String] =
    arbitraryString.filterNot(new File(tempDir, _).exists)

  implicit val arbSong: Gen[Song] = for {
    filePath <- arbitraryFileName
    file = tempDir.addFile(filePath)
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
  private implicit val songShrink: Shrink[Song] = Shrink { song =>
    val shrunkPath = Shrink.shrink(song.file.path).filter(_.nonEmpty)
    val shrunkTitles = Shrink.shrink(song.title)
    val shrunkArtistNames = Shrink.shrink(song.artistName)
    val shrunkAlbumNames = Shrink.shrink(song.albumName)
    val shrunkYears = Shrink.shrink(song.year)
    val shrunkBitRates = Shrink.shrink(song.bitRate)
    val shrunkDurations = Shrink.shrink(song.duration)
    val shrunkSizes = Shrink.shrink(song.size)
    val shrunkDiscNumbers = Shrink.shrink(song.discNumber)
    val shrunkTrackGains = Shrink.shrink(song.trackGain)
    val shrunkComposers = Shrink.shrink(song.composer)
    val shrunkConductors = Shrink.shrink(song.conductor)
    val shrunkOrchestras = Shrink.shrink(song.orchestra)
    val shrunkOpuses = Shrink.shrink(song.opus)
    val shrunkPerformanceYears = Shrink.shrink(song.performanceYear)
    for {
      newPath <- shrunkPath
      newFile = {
        new File(tempDir, newPath).delete
        tempDir.addFile(newPath)
      }
      newTitle <- shrunkTitles
      newArtistName <- shrunkArtistNames
      newAlbumName <- shrunkAlbumNames
      newTrack = song.trackNumber
      newYear <- shrunkYears
      newBitRate <- shrunkBitRates
      newDuration <- shrunkDurations
      newSize <- shrunkSizes
      newDiscNumber <- shrunkDiscNumbers
      newTrackGain <- shrunkTrackGains
      newComposer <- shrunkComposers
      newConductor <- shrunkConductors
      newOrchestra <- shrunkOrchestras
      newOpus <- shrunkOpuses
      newPerformanceYear <- shrunkPerformanceYears
    } yield IOSong(
      newFile,
      newTitle,
      newArtistName,
      newAlbumName,
      newTrack,
      newYear,
      newBitRate,
      newDuration,
      newSize,
      newDiscNumber,
      newTrackGain,
      newComposer,
      newConductor,
      newOrchestra,
      newOpus,
      newPerformanceYear,
    )
  }
  private implicit def genToArb[T: Gen]: Arbitrary[T] = Arbitrary(implicitly[Gen[T]])
  implicit lazy val arbAlbumDir: Gen[AlbumDir] = {
    val dir = TempDirectory()
    for {
      title <- arbitraryString
      artistName <- arbitraryString
      year <- arbitrary[Int].map(_ % 3000)
      songs <- containerOfN[Seq, Song](Gen.choose(1, 10))
    } yield AlbumDir(dir, title, artistName, year, songs)
  }
  implicit val shrinkAlbumDir: Shrink[AlbumDir] = Shrink { album =>
    val shrunkTitles = Shrink.shrink(album.title)
    val shrunkArtistNames = Shrink.shrink(album.artistName)
    val shrunkYears = Shrink.shrink(album.year)
    val shrunkSongs = Shrink.shrink(album.songs)
    for {
      newTitle <- shrunkTitles
      newArtistName <- shrunkArtistNames
      newYear <- shrunkYears
      newSongs <- shrunkSongs
    } yield AlbumDir(album.dir, newTitle, newArtistName, newYear, newSongs)
  }
  implicit lazy val arbArtist: Gen[ArtistDir] = {
    val dir = TempDirectory()
    for {
      name <- arbitraryString
      albums <- containerOfN[Set, AlbumDir](Gen.choose(1, 10))
    } yield ArtistDir(dir, name, albums)
  }
  implicit val shrinkArtistDir: Shrink[ArtistDir] = Shrink { artist =>
    val shrunkNames = Shrink.shrink(artist.name)
    val shrunkAlbums = Shrink.shrink(artist.albums)
    for {
      newName <- shrunkNames
      newAlbums <- shrunkAlbums
    } yield ArtistDir(artist.dir, newName, newAlbums.toSet)
  }

  // TODO move to ScalaCommon
  private def containerOfN[C[_], A: Arbitrary](n: Gen[Int])(implicit
      evb: Buildable[A, C[A]],
      evt: C[A] => Iterable[A],
  ): Gen[C[A]] = n.flatMap(Gen.containerOfN[C, A](_, arbitrary[A]))
}
