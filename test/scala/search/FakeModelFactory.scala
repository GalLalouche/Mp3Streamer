package search

import java.io.File
import java.util.UUID

import common.io.{IODirectory, IOFile}
import models._

class FakeModelFactory {
  def album(dir: File = new File("./foobar"), title: String = "album", artistName: String = "artist",
            year: Int = 2000) =
    new Album(new IODirectory(dir.getAbsoluteFile), title = title, artistName = artistName, year = year)
  def song(filePath: String = s"./foobar/${UUID.randomUUID()}.mp3", title: String = "title", artistName: String = "artist",
           albumName: String = "album", track: Int = 1, year: Int = 2000, bitRate: String = "320",
           duration: Int = 3600, size: Long = 1000, album: Album = null, discNumber: Option[String] = None,
           trackGain: Option[Double] = None): Song = {
    val _album = album
    val file = new File(filePath).getAbsoluteFile
    new IOSong(IOFile(file), title, artistName,
      Option(album).map(_.title).getOrElse(albumName), track, year, bitRate, duration, size,
      discNumber, trackGain) {
      override lazy val album = _album
    }
  }
  def artist(name: String = "artist", albums: Seq[Album] = Seq()): Artist = Artist(name, albums.toSet)
}
