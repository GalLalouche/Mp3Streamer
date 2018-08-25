package models

import backend.configs.Configuration
import common.io.DirectoryRef
import monocle.macros.Lenses
import net.codingwell.scalaguice.InjectorExtensions._

@Lenses
case class Album(dir: DirectoryRef, title: String, artistName: String, year: Int, songs: Seq[Song])

object Album {
  def apply(dir: DirectoryRef)(implicit c: Configuration): Album = {
    val mf = c.injector.instance[MusicFinder]
    val songs = mf.getSongsInDir(dir).ensuring(_.nonEmpty, s"Cannot create an album of an empty dir <$dir>")
    val firstSong = songs.head
    new Album(dir = dir,
      title = firstSong.albumName,
      artistName = firstSong.artistName,
      year = firstSong.year,
      songs = songs)
  }
}
