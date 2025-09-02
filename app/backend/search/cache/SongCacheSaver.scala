package backend.search.cache

import com.google.inject.Inject
import models.{AlbumDir, ArtistDir, Song}
import musicfinder.ArtistDirsIndex

import common.io.JsonableSaver
import common.json.Jsonable
import common.rich.RichT.richT

/**
 * Saves the song cache in individual files, adapting its format to the one used by the index
 * builder.
 */
private class SongCacheSaver @Inject() (
    jsonableSaver: JsonableSaver,
    artistDirsIndexState: ArtistDirsIndex,
) {
  def apply(songs: Iterable[Song])(implicit
      songJsonable: Jsonable[Song],
      albumJsonable: Jsonable[AlbumDir],
      artistJsonable: Jsonable[ArtistDir],
  ): Unit = {
    jsonableSaver.saveArray(songs)
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
      .toSet <| jsonableSaver.saveArray

    val artists = albums.groupBy(_.toTuple(_.dir.parent, _.artistName)).map {
      case ((dir, artistName), albums) => ArtistDir(dir, artistName, albums)
    }
    artistDirsIndexState.update(artists)
    jsonableSaver.saveArray[ArtistDir](artists)
  }
}
