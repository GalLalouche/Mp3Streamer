package models

import common.io.DirectoryRef
import monocle.macros.Lenses

@Lenses
case class Album(dir: DirectoryRef, title: String, artistName: String, year: Int, songs: Seq[Song])

object Album {
  def apply(dir: DirectoryRef)(implicit mfp: MusicFinderProvider): Album = {
    val songs = mfp.mf.getSongsInDir(dir).ensuring(_.nonEmpty, s"Cannot create an album of an empty dir <$dir>")
    val firstSong = songs.head
    new Album(dir = dir,
      title = firstSong.albumName,
      artistName = firstSong.artistName,
      year = firstSong.year,
      songs = songs)
  }
}
