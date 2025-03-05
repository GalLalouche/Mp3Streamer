package songs.selector

import com.google.inject.Inject

import models.Song
import musicfinder.MusicFinder

import scala.annotation.tailrec
import scala.util.Random

import common.TimedLogger
import common.io.DirectoryRef
import common.rich.RichRandomSpecVer.richRandomSpecVer

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
    @tailrec def go(dir: DirectoryRef): Song = {
      val dirs = dir.dirs
      if (dirs.isEmpty) {
        val songs = mf.getSongsInDir(dir).toVector
        if (songs.isEmpty)
          throw new NoSuchElementException(s"No songs in dir without subdirectories <$dir>")
        random.select(songs)
      } else
        go(random.select(dirs.toVector))
    }
    try
      go(random.select(mf.genreDirsWithSubGenres.toVector))
    catch {
      case _: NoSuchElementException => randomSong()
    }
  }
}
