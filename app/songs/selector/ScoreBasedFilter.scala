package songs.selector

import backend.recon.Track
import backend.score.{AggregateScorer, ScoreBasedProbability}

import scala.util.Random

import common.Filter

private class ScoreBasedFilter(
    random: Random,
    scorer: AggregateScorer,
    scoreBasedProbability: ScoreBasedProbability,
) extends Filter[Track] {
  override def passes(track: Track): Boolean = {
    val percentage = scoreBasedProbability(track)
    val aggregateScore = scorer.aggregateScore(track)
    val score = aggregateScore.toOptionalModelScore
    val source = aggregateScore.source.getOrElse("N/A")
    val shortSongString = s"${track.album.artist} - ${track.title} (${score.entryName}, $source)"
    val $ = percentage.roll(random)
    if ($) scribe.trace(s"Chose song <$shortSongString> with probability $percentage")
    else scribe.trace(s"Skipped song <$shortSongString> with probability ${percentage.inverse}")
    $
  }
}
