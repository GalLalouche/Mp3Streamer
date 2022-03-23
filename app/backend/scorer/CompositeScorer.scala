package backend.scorer

import backend.recon.{Album, Artist}
import backend.recon.Reconcilable.SongExtractor
import backend.scorer.ModelScorer.{SongScore, Source}
import backend.scorer.ModelScorer.SongScore.Scored
import models.Song

import scala.language.higherKinds

import scalaz.{Bind, OptionT}
import scalaz.syntax.bind._

/** Scores a song by trying multiple sources, from most specific score to least specific. */
private class CompositeScorer[M[_] : Bind](
    // TODO handle live/studio version differences using two different scorer: one uses album one doesn't
    // TODO Also covers, by ignoring artist? This could lead to a further linking of multiple songs to a single source thereby creating my own private MusicBrainz :\
    songScorer: Song => OptionT[M, ModelScore],
    albumScorer: Album => OptionT[M, ModelScore],
    artistScorer: Artist => OptionT[M, ModelScore],
) {
  def apply(s: Song): M[SongScore] = for {
    songScore <- songScorer(s).run
    albumScore <- albumScorer(s.release).run
    artistScore <- artistScorer(s.artist).run
  } yield {
    songScore.map(
      Scored(_,
        Source.Song,
        songScore.getOrElse(ModelScore.Default),
        albumScore.getOrElse(ModelScore.Default),
        artistScore.getOrElse(ModelScore.Default),
      )).orElse(
      albumScore.map(
        Scored(_,
          Source.Album,
          songScore.getOrElse(ModelScore.Default),
          albumScore.getOrElse(ModelScore.Default),
          artistScore.getOrElse(ModelScore.Default),

        ))).orElse(
      artistScore.map(
        Scored(_,
          Source.Artist,
          songScore.getOrElse(ModelScore.Default),
          albumScore.getOrElse(ModelScore.Default),
          artistScore.getOrElse(ModelScore.Default),
        )
      )).getOrElse(SongScore.Default)
  }
}
