package songs.selector

import javax.inject.Inject

import models.Song
import musicfinder.MusicFinder

import scala.annotation.tailrec
import scala.util.Random

import common.TimedLogger
import common.io.DirectoryRef
import common.rich.RichRandom.richRandom

/**
 * Sacrifices uniform distribution for lower latency while waiting for update to complete (since
 * loading TBs of songs and scoring them takes a while apparently).
 */
private class FastSongSelector @Inject() (
    mf: MusicFinder,
    timedLogger: TimedLogger,
    random: Random,
) extends SongSelector {
  final override def randomSong(): Song = timedLogger.apply("fastRandomSong", scribe.debug(_)) {
    @tailrec def go(dir: DirectoryRef): Song =
      if (dir.dirs.isEmpty) {
        val songs = mf.getSongsInDir(dir).toVector
        if (songs.isEmpty)
          throw new NoSuchElementException(s"No songs in dir without subdirectories <$dir>")
        random.select(songs)
      } else
        go(random.select(dir.dirs.toVector))
    try
      go(random.select(mf.genreDirsWithSubGenres.toVector))
    catch {
      case _: NoSuchElementException => randomSong()
    }
  }
}
