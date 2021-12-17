package backend.scorer

import backend.recon.{Album, Artist}
import backend.recon.Reconcilable.SongExtractor
import backend.scorer.ModelScorer.{SongScore, Source}
import models.Song

import scala.language.higherKinds

import scalaz.{Monad, OptionT}

/** Scores a song by trying multiple sources, from most specific score to least specific. */
private class CompositeScorer[M[_] : Monad](
    // TODO handle live/studio version differences using two different scorer: one uses album one doesn't
    // TODO Also covers, by ignoring artist? This could lead to a further linking of multiple songs to a single source thereby creating my own private MusicBrainz :\
    songScore: Song => OptionT[M, ModelScore],
    albumScore: Album => OptionT[M, ModelScore],
    artistScore: Artist => OptionT[M, ModelScore],
) {
  def apply(s: Song): M[SongScore] =
    songScore(s).map(Source.Song.apply) |||
        albumScore(s.release).map(Source.Album.apply) |||
        artistScore(s.artist).map(Source.Artist.apply) |
        SongScore.Default
}
