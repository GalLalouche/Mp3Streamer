package backend.search.cache

import javax.inject.Inject

import models.{Album, Artist, Song}

import common.TimedLogger
import common.io.{IODirectory, IOFile, JsonableSaver}
import common.json.Jsonable
import common.rich.RichT.richT

/**
 * Saves the song cache in individual files, adapting its format to the one used by the index
 * builder.
 */
private class SongCacheSplitter @Inject() (
    jsonableSaver: JsonableSaver,
    timedLogger: TimedLogger,
) {
  def apply(cs: SongCache)(implicit
      songJsonable: Jsonable[Song],
      albumJsonable: Jsonable[Album],
      artistJsonable: Jsonable[Artist],
  ): Unit = {
    val songs = cs.songs <| jsonableSaver.saveArray
    val albums =
      songs
        .groupBy(_.file.asInstanceOf[IOFile].file.getParent)
        .map { case (parent, songs) =>
          val firstSong = songs.head
          Album(
            IODirectory(parent),
            firstSong.albumName,
            firstSong.artistName,
            firstSong.year,
            songs.toVector,
          )
        }
        .toSet <| jsonableSaver.saveArray

    val artists = albums.groupBy(_.artistName).map(Function.tupled(Artist.apply))
    jsonableSaver.saveArray[Artist](artists)
  }
}
