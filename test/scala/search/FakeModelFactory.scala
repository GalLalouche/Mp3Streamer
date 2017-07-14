package search

import java.util.UUID

import common.io.MemoryRoot
import models._

class FakeModelFactory {
  private val root = new MemoryRoot
  def album(dirName: String = UUID.randomUUID().toString, title: String = "album",
      artistName: String = "artist", year: Int = 2000) =
    new Album(root addSubDir dirName, title = title, artistName = artistName, year = year)
  def song(filePath: String = s"${UUID.randomUUID()}.mp3", title: String = "title", artistName: String = "artist",
      albumName: String = "album", track: Int = 1, year: Int = 2000, bitRate: String = "320",
      duration: Int = 3600, size: Long = 1000, discNumber: Option[String] = None,
      trackGain: Option[Double] = None): MemorySong =
    MemorySong(root.addFile(filePath), title, artistName, albumName, track, year, bitRate, duration, size, discNumber,
      trackGain)
  def artist(name: String = "artist", albums: Seq[Album] = Seq()): Artist = Artist(name, albums.toSet)
}
