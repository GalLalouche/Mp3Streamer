package songs.selector

import backend.recon.Reconcilable.SongExtractor
import backend.score.{AggregateScorer, ScoreBasedProbability}
import com.google.inject.Inject
import models.Song

import scala.util.Random

import common.Filter

private class ScoreBasedFilter @Inject() (
    random: Random,
    scorer: AggregateScorer,
    scoreBasedProbability: ScoreBasedProbability,
) extends Filter[Song] {
  override def passes(song: Song): Boolean = {
    val percentage = scoreBasedProbability(song)
    val aggregateScore = scorer.aggregateScore(song.track)
    val score = aggregateScore.toOptionalModelScore
    val source = aggregateScore.source.getOrElse("N/A")
    val shortSongString = s"${song.artistName} - ${song.title} (${score.entryName}, $source)"
    val $ = percentage.roll(random)
    if ($) scribe.trace(s"Chose song <$shortSongString> with probability $percentage")
    else scribe.trace(s"Skipped song <$shortSongString> with probability ${percentage.inverse}")
    $
  }
}
