package songs.selector

import backend.logging.Logger
import backend.scorer.{CachedModelScorer, ScoreBasedProbability}
import javax.inject.Inject
import models.Song

import scala.util.Random

import common.Filter

private class ScoreBasedFilter @Inject()(
    random: Random,
    cachedModelScorer: CachedModelScorer,
    scoreBasedProbability: ScoreBasedProbability,
    logger: Logger,
) extends Filter[Song] {
  override def passes(song: Song): Boolean = {
    val percentage = scoreBasedProbability(song)
    val score = cachedModelScorer(song)
    val shortSongString = s"${song.artistName} - ${song.title} (${score.orDefaultString})"
    val $ = percentage.roll(random)
    if ($) logger.debug(s"Chose song <$shortSongString> with probability $percentage")
    else logger.debug(s"Skipped song <$shortSongString> with probability ${percentage.inverse}")
    $
  }
}
