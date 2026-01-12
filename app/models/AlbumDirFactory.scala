package models

import com.google.inject.Inject
import musicfinder.SongDirectoryParser

import common.io.DirectoryRef
import common.rich.RichT.richT
import common.rich.collections.RichIterator.richIterator

class AlbumDirFactory @Inject() (songDirectoryParser: SongDirectoryParser) {
  def fromDir(dir: DirectoryRef): AlbumDir = {
    val songs =
      songDirectoryParser(dir)
        .requiring(_.nonEmpty, s"Cannot create an album of an empty dir <$dir>")
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
    songs = songDirectoryParser(s.file.parent).toVector,
  )
}
