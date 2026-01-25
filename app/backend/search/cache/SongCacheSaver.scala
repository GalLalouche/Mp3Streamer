package backend.search.cache

import com.google.inject.Inject
import models.{AlbumDir, ArtistDir, Song}
import musicfinder.ArtistDirsIndex

import common.AvroableSaver
import common.io.avro.Avroable
import common.rich.RichT.richT

/**
 * Saves the song cache in individual files, adapting its format to the one used by the index
 * builder.
 */
private class SongCacheSaver @Inject() (
    saver: AvroableSaver,
    artistDirsIndexState: ArtistDirsIndex,
) {
  def apply(songs: Iterable[Song])(implicit
      songJsonable: Avroable[Song],
      albumJsonable: Avroable[AlbumDir],
      artistJsonable: Avroable[ArtistDir],
  ): Unit = {
    saver.save(songs)
    val albums = songs
      .groupBy(_.file.parent)
      .map { case (parent, songs) =>
        val firstSong = songs.head
        AlbumDir(
          parent,
          firstSong.albumName,
          firstSong.artistName,
          firstSong.year,
          songs.toVector,
        )
      }
      .toSet <| saver.save

    val artists = albums
      .groupBy(_.toTuple(_.dir.parent, _.artistName))
      .map { case ((dir, artistName), albums) =>
        ArtistDir(dir, artistName, albums)
      }
      .toVector
      .sortBy(_.toTuple(_.dir.parent.path, _.name))
    artistDirsIndexState.update(artists)
    saver.save(artists)
  }
}
