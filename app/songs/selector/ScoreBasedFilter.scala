package songs.selector

import backend.recon.Track
import backend.score.{AggregateScorer, ScoreBasedProbability}

import scala.util.Random

import common.Percentage

private class ScoreBasedFilter(
    random: Random,
    scorer: AggregateScorer,
    scoreBasedProbability: ScoreBasedProbability,
) extends RandomFilterTemplate(random) {
  protected override def aux(track: Track): (Percentage, String) = {
    val aggregateScore = scorer.aggregateScore(track)
    val score = aggregateScore.toOptionalModelScore
    val source = aggregateScore.source.getOrElse("N/A")
    val description = s"(${score.entryName}, $source)"
    (scoreBasedProbability(track), description)
  }
}
