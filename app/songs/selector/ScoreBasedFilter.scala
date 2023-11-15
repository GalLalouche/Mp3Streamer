package songs.selector

import javax.inject.Inject
import scala.util.Random

import backend.logging.Logger
import backend.scorer.{CachedModelScorer, ScoreBasedProbability}
import backend.scorer.FullInfoScore.Scored
import common.rich.RichT.richT
import common.Filter
import models.Song

private class ScoreBasedFilter @Inject() (
    random: Random,
    cachedModelScorer: CachedModelScorer,
    scoreBasedProbability: ScoreBasedProbability,
    logger: Logger,
) extends Filter[Song] {
  override def passes(song: Song): Boolean = {
    val percentage = scoreBasedProbability(song)
    val fullInfoScore = cachedModelScorer.fullInfo(song)
    val score = fullInfoScore.toOptionalModelScore
    val source = fullInfoScore.safeCast[Scored].map(_.source).getOrElse("N/A")
    val shortSongString = s"${song.artistName} - ${song.title} (${score.entryName}, $source)"
    val $ = percentage.roll(random)
    if ($) logger.verbose(s"Chose song <$shortSongString> with probability $percentage")
    else logger.verbose(s"Skipped song <$shortSongString> with probability ${percentage.inverse}")
    $
  }
}
