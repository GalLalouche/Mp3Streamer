package songs.selector

import backend.recon.Reconcilable.SongExtractor
import backend.scorer.{CachedModelScorer, FullInfoScore, ModelScore, ScoreSource}
import models.{Genre, GenreFinder, Song}

import scala.concurrent.duration.Duration
import scala.math.Ordering.Implicits._

import common.Filter
import common.rich.RichEnumeratum.richEnumeratum

private class LengthFilter(
    genreFinder: GenreFinder,
    scorer: CachedModelScorer,
    minLength: Duration,
) extends Filter[Song] {
  private implicit val ordering: Ordering[ModelScore] = ModelScore.ordering
  override def passes(song: Song): Boolean = genreFinder.forArtist(song.artist) match {
    case Some(Genre.Metal(_)) => song.duration >= minLength || songScoreIsGood(song)
    case _ => true
  }

  // If a specific song has been explicitly scored, it overrides the length requirements.
  private def songScoreIsGood(song: Song): Boolean = scorer.fullInfo(song) match {
    case FullInfoScore.Default => false
    case FullInfoScore.Scored(_, source, _, _, _) => source == ScoreSource.Song
  }
}
