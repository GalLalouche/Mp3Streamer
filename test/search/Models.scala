package search

import java.io.File

import models.{Album, Artist, Song}

private object Models {
  def mockSong(file: String = "./foobar/song", title: String = "title", artistName: String = "artist",
               albumName: String = "album", track: Int = 1, year: Int = 2000, bitrate: String = "320",
               duration: Int = 3600, size: Long = 1000, album: Album = null): Song = {
    val _album = album
    new Song(new File(file).getAbsoluteFile, title, artistName, albumName, track, year, bitrate, duration, size) {
      override lazy val album = _album
    }
  }
  def mockAlbum(dir: File = new File("./foobar"), title: String = "album", artistName: String = "artist",
                year: Int = 2000) =
    new Album(new File("foobar").getAbsoluteFile, title = title, artistName = artistName, year = year)
  def mockArtist(name: String = "artist", albums: Seq[Album] = Seq()): Artist = new Artist(name, albums.toSet)
}
