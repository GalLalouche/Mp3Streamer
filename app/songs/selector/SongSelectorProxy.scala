package songs.selector

import backend.logging.{Logger, LoggingLevel}
import backend.scorer.{CachedModelScorer, ScoreBasedProbability}
import com.google.inject.Provider
import javax.inject.Inject
import models.{MusicFinder, Song}

import scala.annotation.tailrec
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

import scalaz.syntax.functor.ToFunctorOps
import common.rich.func.BetterFutureInstances._

import common.io.DirectoryRef
import common.rich.RichFuture._
import common.rich.RichRandom.richRandom
import common.TimedLogger

/** A mutable-updateable wrapper of SongSelector */
private class SongSelectorProxy @Inject()(
    ec: ExecutionContext,
    mf: MusicFinder,
    scoreBasedProbability: Provider[ScoreBasedProbability],
    cachedModelScorer: Provider[CachedModelScorer],
    logger: Logger,
    timedLogger: TimedLogger,
) extends SongSelector {
  private implicit val iec: ExecutionContext = ec
  private var songSelectorFuture: Future[SongSelector] = _
  def update(): Future[_] = {
    val $ = Future(new SongSelectorImpl(
      mf.getSongFiles.toVector)(
      mf, scoreBasedProbability.get(), cachedModelScorer.get(), logger))
    if (songSelectorFuture == null) songSelectorFuture = $
    else $.>|(songSelectorFuture = $) // Don't override until complete.
    $
  }
  private lazy val ss = songSelectorFuture.get
  override def randomSong() = if (songSelectorFuture.isCompleted) ss.randomSong() else fastRandomSong()
  // We sacrifice uniform distribution for lower latency while waiting for update to complete (since loading
  // TBs of songs and scoring them takes a while apparently).
  private def fastRandomSong(): Song = timedLogger.apply("fastRandomSong", LoggingLevel.Debug) {
    val random = Random
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
      case _: NoSuchElementException => fastRandomSong()
    }
  }
}
