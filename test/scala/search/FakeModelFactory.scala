package search

import java.util.UUID

import common.io.MemoryRoot
import models._

class FakeModelFactory {
  private val root = new MemoryRoot
  def album(dirName: String = UUID.randomUUID().toString, title: String = "album",
            artistName: String = "artist", year: Int = 2000) =
    new Album(root addSubDir dirName, title = title, artistName = artistName, year = year)
  // Returns a Song instead of MemorySong for Jsonable typeclassing, since Jsonable is invariant
  def song(filePath: String = s"${UUID.randomUUID()}.mp3", title: String = "title", artistName: String = "artist",
           albumName: String = "album", track: Int = 1, year: Int = 2000, bitRate: String = "320",
           duration: Int = 3600, size: Long = 1000, album: Album = null, discNumber: Option[String] = None,
           trackGain: Option[Double] = None): MemorySong = {
    val _album = album
    new MemorySong(root.addFile(filePath), title, artistName,
      Option(album).map(_.title).getOrElse(albumName), track, year, bitRate, duration, size,
      discNumber, trackGain) {
      override lazy val album = _album
    }
  }
  def artist(name: String = "artist", albums: Seq[Album] = Seq()): Artist = Artist(name, albums.toSet)
}
