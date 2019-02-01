package models

import java.util.UUID

import common.io.MemoryRoot
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary

class FakeModelFactory {
  private val root = new MemoryRoot
  def album(dirName: String = UUID.randomUUID().toString, title: String = "album",
      artistName: String = "artist", year: Int = 2000, songs: Seq[Song] = Seq()) =
    Album(root addSubDir dirName, title = title, artistName = artistName, year = year, songs = songs)
  def song(filePath: String = s"${UUID.randomUUID()}.mp3", title: String = "title",
      artistName: String = "artist", albumName: String = "album", track: Int = 1, year: Int = 2000,
      bitRate: String = "320", duration: Int = 3600, size: Long = 1000, discNumber: Option[String] = None,
      trackGain: Option[Double] = None, composer: Option[String] = None, opus: Option[String] = None,
      performanceYear: Option[Int] = None,
  ): MemorySong =
    MemorySong(root.addFile(filePath), title, artistName, albumName, track, year, bitRate, duration, size,
      discNumber, trackGain, composer, opus, performanceYear)
  def artist(name: String = "artist", albums: Seq[Album] = Seq()): Artist = Artist(name, albums.toSet)
  implicit val arbSong: Arbitrary[MemorySong] = Arbitrary(for {
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
  } yield song(
    title = title, artistName = artistName, albumName = albumName, track = track, year = year,
    bitRate = bitRate, duration = duration, size = size, discNumber = discNumber, trackGain = trackGain))
}
