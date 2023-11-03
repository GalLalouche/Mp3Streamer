package backend.search.cache

import backend.logging.LoggingLevel
import javax.inject.Inject
import models.{Album, Artist, Song}

import common.io.{IODirectory, IOFile, JsonableSaver}
import common.json.Jsonable
import common.rich.RichT.richT
import common.TimedLogger

/**
 * Saves the song cache in individual files, adapting its format the one used by the index builder.
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
    def log[A](t: String)(a: A): A = timedLogger(t, LoggingLevel.Info)(a)
    val songs = log("saving songs")(cs.songs <| jsonableSaver.saveArray)
    val albums = log("saving albums")(
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
        .toSet <| jsonableSaver.saveArray,
    )
    log("saving artists") {
      val artists = albums.groupBy(_.artistName).map { case (artistName, albums) =>
        Artist(artistName, albums)
      }
      jsonableSaver.saveArray[Artist](artists)
    }
  }
}
