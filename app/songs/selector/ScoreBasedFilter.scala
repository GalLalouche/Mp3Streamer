package songs.selector

import backend.recon.Reconcilable.SongExtractor
import backend.score.{CachedModelScorer, ScoreBasedProbability}
import backend.score.FullInfoScore.Scored
import com.google.inject.Inject
import models.Song

import scala.util.Random

import common.Filter
import common.rich.RichT.richT

private class ScoreBasedFilter @Inject() (
    random: Random,
    cachedModelScorer: CachedModelScorer,
    scoreBasedProbability: ScoreBasedProbability,
) extends Filter[Song] {
  override def passes(song: Song): Boolean = {
    val percentage = scoreBasedProbability(song)
    val fullInfoScore = cachedModelScorer.fullInfo(song.track)
    val score = fullInfoScore.toOptionalModelScore
    val source = fullInfoScore.safeCast[Scored].map(_.source).getOrElse("N/A")
    val shortSongString = s"${song.artistName} - ${song.title} (${score.entryName}, $source)"
    val $ = percentage.roll(random)
    if ($) scribe.trace(s"Chose song <$shortSongString> with probability $percentage")
    else scribe.trace(s"Skipped song <$shortSongString> with probability ${percentage.inverse}")
    $
  }
}
