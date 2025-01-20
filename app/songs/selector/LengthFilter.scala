package songs.selector

import backend.recon.Reconcilable.SongExtractor
import backend.scorer.{CachedModelScorer, FullInfoScore, ScoreSource}
import models.{Genre, GenreFinder, Song}

import scala.concurrent.duration.Duration

import common.Filter
import common.rich.primitives.RichBoolean.richBoolean

private class LengthFilter(
    genreFinder: GenreFinder,
    scorer: CachedModelScorer,
    minLength: Duration,
) extends Filter[Song] {
  override def passes(song: Song): Boolean = genreFinder.forArtist(song.artist) match {
    // Special exempt for grind subgenres, because, well, you know.
    case Some(Genre.Metal(subgenre)) if subgenre.toLowerCase.contains("grind").isFalse =>
      song.duration >= minLength || hasExplicitSongScore(song)
    case _ => true
  }

  // If a specific song has been explicitly scored, it overrides the length requirements.
  private def hasExplicitSongScore(song: Song): Boolean = scorer.fullInfo(song.track) match {
    case FullInfoScore.Default => false
    case FullInfoScore.Scored(_, source, _, _, _) => source == ScoreSource.Song
  }
}
