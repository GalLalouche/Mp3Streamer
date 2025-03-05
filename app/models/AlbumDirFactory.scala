package models

import com.google.inject.Inject
import musicfinder.MusicFinder

import common.io.DirectoryRef

class AlbumDirFactory @Inject() (mf: MusicFinder) {
  def fromDir(dir: DirectoryRef): AlbumDir = {
    val songs =
      mf.getSongsInDir(dir)
        .ensuring(_.nonEmpty, s"Cannot create an album of an empty dir <$dir>")
        .sortBy(_.trackNumber)
    val firstSong = songs.head
    AlbumDir(
      dir = dir,
      title = firstSong.albumName,
      artistName = firstSong.artistName,
      year = firstSong.year,
      songs = songs,
    )
  }

  implicit class AlbumFactorySongOps(private val $ : Song) {
    def album: AlbumDir = fromSong($)
  }

  private def fromSong(s: Song): AlbumDir = AlbumDir(
    dir = s.file.parent,
    title = s.albumName,
    artistName = s.artistName,
    year = s.year,
    songs = mf.getSongsInDir(s.file.parent),
  )
}
