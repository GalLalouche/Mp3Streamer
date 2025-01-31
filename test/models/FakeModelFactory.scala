package models

import java.util.UUID
import java.util.concurrent.TimeUnit

import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary

import scala.concurrent.duration.Duration

import common.io.MemoryRoot

class FakeModelFactory {
  private val root = new MemoryRoot
  def album(
      dirName: String = UUID.randomUUID().toString,
      title: AlbumTitle = "album",
      artistName: ArtistName = "artist",
      year: Int = 2000,
      songs: Seq[Song] = Nil,
  ) = AlbumDir(
    root.addSubDir(dirName),
    title = title,
    artistName = artistName,
    year = year,
    songs = songs,
  )
  def song(
      filePath: String = s"${UUID.randomUUID()}.mp3",
      title: SongTitle = "title",
      artistName: ArtistName = "artist",
      albumName: AlbumTitle = "album",
      trackNumber: TrackNumber = 1,
      year: Int = 2000,
      bitRate: String = "320",
      duration: Int = 3600,
      size: Long = 1000,
      discNumber: Option[String] = None,
      trackGain: Option[Double] = None,
      composer: Option[String] = None,
      conductor: Option[String] = None,
      orchestra: Option[String] = None,
      opus: Option[String] = None,
      performanceYear: Option[Int] = None,
  ): MemorySong = MemorySong(
    root.addFile(filePath),
    title,
    artistName,
    albumName,
    trackNumber,
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
  def artist(name: ArtistName = "artist", albums: Seq[AlbumDir] = Nil): Artist =
    Artist(name, albums.toSet)
  implicit val arbSong: Arbitrary[MemorySong] = Arbitrary(
    for {
      title <- arbitrary[SongTitle]
      artistName <- arbitrary[String]
      albumName <- arbitrary[AlbumTitle]
      track <- arbitrary[TrackNumber].map(_ % 100)
      year <- arbitrary[Int].map(_ % 3000)
      bitRate <- arbitrary[Int].map(_ % 10000).map(_ / 32.0).map(_.toString)
      duration <- arbitrary[Int].map(_ % 1000)
      size <- arbitrary[Int]
      discNumber <- arbitrary[Option[String]]
      trackGain <- arbitrary[Option[Int]].map(_.map(_ % 10000).map(_ / 32.0))
    } yield song(
      title = title,
      artistName = artistName,
      albumName = albumName,
      trackNumber = track,
      year = year,
      bitRate = bitRate,
      duration = duration,
      size = size,
      discNumber = discNumber,
      trackGain = trackGain,
    ),
  )
}
