package search

import java.io.File
import java.util.UUID

import models.{Album, Artist, Song}

object Models {
  def mockAlbum(dir: File = new File("./foobar"), title: String = "album", artistName: String = "artist",
                year: Int = 2000) =
    new Album(new File("foobar").getAbsoluteFile, title = title, artistName = artistName, year = year)
  def mockSong(file: String = s"./foobar/${UUID.randomUUID()}.mp3", title: String = "title", artistName: String = "artist",
               albumName: String = "album", track: Int = 1, year: Int = 2000, bitrate: String = "320",
               duration: Int = 3600, size: Long = 1000, album: Album = null, discNumber: Option[String] = None,
               trackGain: Option[Double] = None): Song = {
    val _album = album
    new Song(new File(file).getAbsoluteFile, title, artistName,
      Option(album).map(_.title).getOrElse(albumName), track, year, bitrate, duration, size, discNumber, trackGain) {
      override lazy val album = _album
    }
  }
  def mockArtist(name: String = "artist", albums: Seq[Album] = Seq()): Artist = Artist(name, albums.toSet)
}
