package models

import javax.inject.Inject

import common.io.DirectoryRef

class AlbumFactory @Inject() (mf: MusicFinder) {
  def fromSong(s: Song): Album = Album(
    dir = s.file.parent,
    title = s.albumName,
    artistName = s.artistName,
    year = s.year,
    songs = mf.getSongsInDir(s.file.parent),
  )

  def fromDir(dir: DirectoryRef): Album = {
    val songs =
      mf.getSongsInDir(dir)
        .ensuring(_.nonEmpty, s"Cannot create an album of an empty dir <$dir>")
        .sortBy(_.trackNumber)
    val firstSong = songs.head
    Album(
      dir = dir,
      title = firstSong.albumName,
      artistName = firstSong.artistName,
      year = firstSong.year,
      songs = songs,
    )
  }

  implicit class AlbumFactorySongOps(private val $ : Song) {
    def album: Album = fromSong($)
  }
}
