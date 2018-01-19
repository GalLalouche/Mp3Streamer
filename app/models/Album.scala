package models

import common.io.DirectoryRef
import monocle.Getter

case class Album(dir: DirectoryRef, title: String, artistName: String, year: Int, songs: Seq[Song])

object Album {
  def apply(dir: DirectoryRef)(implicit mf: MusicFinder): Album = {
    val songs = mf.getSongsInDir(dir).ensuring(_.nonEmpty, s"Cannot create an album of an empty dir <$dir>")
    val firstSong = songs.head
    new Album(dir = dir,
      title = firstSong.albumName,
      artistName = firstSong.artistName,
      year = firstSong.year,
      songs = songs)
  }
  def songs(implicit mf: MusicFinder): Getter[Album, Seq[Song]] = Getter(_.songs)
}
