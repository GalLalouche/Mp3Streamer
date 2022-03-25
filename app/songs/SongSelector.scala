package songs

import backend.logging.Logger
import backend.scorer.ScoreBasedProbability
import javax.inject.Inject
import models.{MusicFinder, Song}

import scala.annotation.tailrec
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

import scalaz.syntax.functor.ToFunctorOps
import common.rich.func.BetterFutureInstances._

import common.io.RefSystem
import common.rich.RichRandom.richRandom
import common.rich.RichT._

trait SongSelector {
  def randomSong(): Song
  @tailrec
  private def randomSongWithExtension(ext: String): Song = {
    val $ = randomSong()
    if ($.file.extension == ext) $ else randomSongWithExtension(ext)
  }
  def randomMp3Song(): Song = randomSongWithExtension("mp3")
  def randomFlacSong(): Song = randomSongWithExtension("flac")
  def followingSong(song: Song): Option[Song]
}

private class SongSelectorImpl[Sys <: RefSystem](
    songs: IndexedSeq[Sys#F])(
    musicFinder: MusicFinder {type S = Sys},
    scoreBasedProbability: ScoreBasedProbability,
    logger: Logger,
)
    extends SongSelector {
  private val random = new Random()
  @tailrec final def randomSong(): Song = {
    val song = musicFinder.parseSong(random.select(songs))
    val percentage = scoreBasedProbability(song)
    if (percentage.roll(random)) {
      logger.debug(s"Chose song <$song> with probability ${percentage.inverse}")
      song
    } else {
      logger.debug(s"Skipped song <$song> with probability ${1 - percentage.p}")
      randomSong()
    }
  }
  def followingSong(song: Song): Option[Song] =
    song.file.parent
        .|>(musicFinder.getSongsInDir)
        .sortBy(_.track)
        .lift(song.track)
}

private object SongSelector {
  import common.rich.RichFuture._

  /** A mutable-updateable wrapper of SongSelector */
  class SongSelectorProxy @Inject()(
      ec: ExecutionContext,
      mf: MusicFinder,
      scoreBasedProbability: ScoreBasedProbability,
      logger: Logger,
  ) extends SongSelector {
    private implicit val iec: ExecutionContext = ec
    def update(): Future[_] = {
      val $ = Future(new SongSelectorImpl(mf.getSongFiles.toVector)(mf, scoreBasedProbability, logger))
      if (songSelector == null)
        songSelector = $
      else // don't override until complete
        $.>|(songSelector = $)
      $
    }
    private var songSelector: Future[SongSelector] = _
    private lazy val ss = songSelector.get
    override def randomSong() = ss.randomSong
    override def followingSong(song: Song) = ss followingSong song
  }
}
