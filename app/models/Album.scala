package models

import common.io.DirectoryRef
import monocle.Getter

// TODO make songs a class field, and transform Song.album to require an implicit MF
case class Album(dir: DirectoryRef, title: String, artistName: String, year: Int) {
  def songs(implicit mf: MusicFinder): Seq[Song] = mf.getSongsInDir(dir)
}

object Album {
  def apply(dir: DirectoryRef)(implicit mf: MusicFinder) = {
    val firstSong = mf.getSongsInDir(dir).head
    new Album(dir = dir,
      title = firstSong.albumName,
      artistName = firstSong.artistName,
      year = firstSong.year)
  }
  def songs(implicit mf: MusicFinder): Getter[Album, Seq[Song]] = Getter.apply(_.songs)
}
