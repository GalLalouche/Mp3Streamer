package songs.selector

import backend.logging.Logger
import backend.scorer.{CachedModelScorer, ScoreBasedProbability}
import models.{MusicFinder, Song}

import scala.annotation.tailrec
import scala.util.Random

import common.io.RefSystem
import common.rich.RichRandom.richRandom
import common.rich.RichT.richT

private class SongSelectorImpl[Sys <: RefSystem](
    songs: IndexedSeq[Sys#F])(
    musicFinder: MusicFinder,
    // FIXME This isn't updated when the score changes until the next reset.
    scoreBasedProbability: ScoreBasedProbability,
    cachedModelScorer: CachedModelScorer,
    logger: Logger,
) extends SongSelector {
  private val random = new Random()
  @tailrec final def randomSong(): Song = {
    val song = musicFinder.parseSong(random.select(songs))
    val percentage = scoreBasedProbability(song)
    val score = cachedModelScorer(song)
    val shortSongString = s"${song.artistName} - ${song.title} (${score.orDefaultString})"
    if (percentage.roll(random)) {
      logger.debug(s"Chose song <$shortSongString> with probability $percentage")
      song
    } else {
      logger.debug(s"Skipped song <$shortSongString> with probability ${percentage.inverse}")
      randomSong()
    }
  }
}
