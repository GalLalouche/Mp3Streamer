package songs.selector

import backend.logging.LoggingLevel
import javax.inject.Inject
import models.{MusicFinder, Song}

import scala.annotation.tailrec
import scala.util.Random

import common.io.DirectoryRef
import common.rich.RichRandom.richRandom
import common.TimedLogger

/**
* Sacrifices uniform distribution for lower latency while waiting for update to complete (since loading
* TBs of songs and scoring them takes a while apparently).
*/
private class FastSongSelector @Inject()(
    mf: MusicFinder,
    timedLogger: TimedLogger,
    random: Random,
) extends SongSelector {
  override final def randomSong(): Song = timedLogger.apply("fastRandomSong", LoggingLevel.Debug) {
    @tailrec def go(dir: DirectoryRef): Song = {
      if (dir.dirs.isEmpty) {
        val songs = mf.getSongsInDir(dir).toVector
        if (songs.isEmpty)
          throw new NoSuchElementException(s"No songs in dir without subdirectories <$dir>")
        random.select(songs)
      } else
        go(random.select(dir.dirs.toVector))
    }
    try {
      go(random.select(mf.genreDirsWithSubGenres.toVector))
    } catch {
      case _: NoSuchElementException => randomSong()
    }
  }
}