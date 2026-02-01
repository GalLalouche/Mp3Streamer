package models

import com.google.inject.Inject
import musicfinder.SongDirectoryParser

import common.path.ref.DirectoryRef
import common.rich.RichT.richT
import common.rich.collections.RichIterator.richIterator
import common.rich.primitives.RichOption.richOption

class AlbumDirFactory @Inject() (songDirectoryParser: SongDirectoryParser) {
  def fromDir(dir: DirectoryRef): AlbumDir = {
    val songs =
      songDirectoryParser(dir)
        .requiring(_.nonEmpty, s"Cannot create an album of an empty dir <$dir>")
        .sortBy(_.trackNumber)

    fromDir(dir, songs.head, songs)
  }
  def fromDirWithoutSongs(dir: DirectoryRef): AlbumDir = {
    val firstSong = songDirectoryParser(dir).headOption.getOrThrow(
      s"Cannot create an album of an empty dir <$dir>",
    )

    fromDir(dir, firstSong, Vector.empty)
  }

  private def fromDir(dir: DirectoryRef, firstSong: Song, songs: Seq[Song]) = AlbumDir(
    dir = dir,
    title = firstSong.albumName,
    artistName = firstSong.artistName,
    year = firstSong.year,
    songs = songs,
  )

  implicit class AlbumFactorySongOps(private val $ : Song) {
    def album: AlbumDir = AlbumDir(
      dir = $.file.parent,
      title = $.albumName,
      artistName = $.artistName,
      year = $.year,
      songs = songDirectoryParser($.file.parent).toVector,
    )
  }
}
