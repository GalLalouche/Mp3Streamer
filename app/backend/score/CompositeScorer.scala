package backend.score

import backend.recon.{Album, Artist, Track}

import cats.FlatMap
import cats.data.OptionT
import cats.syntax.flatMap.toFlatMapOps
import cats.syntax.functor.toFunctorOps

/** Scores a song by trying multiple sources, from most specific score to least specific. */
private class CompositeScorer[M[_]: FlatMap](
    // TODO handle live/studio version differences using two different scorer: one uses album one doesn't
    // TODO Also covers, by ignoring artist? This could lead to a further linking of multiple songs to a single source thereby creating my own private MusicBrainz :\
    songScorer: Track => OptionT[M, ModelScore],
    albumScorer: Album => OptionT[M, ModelScore],
    artistScorer: Artist => OptionT[M, ModelScore],
) {
  def apply(t: Track): M[FullInfoScore] = for {
    songScore <- songScorer(t).value
    albumScore <- albumScorer(t.album).value
    artistScore <- artistScorer(t.artist).value
  } yield {
    def makeScored(source: ScoreSource)(score: ModelScore) = FullInfoScore.Scored(
      score,
      source,
      songScore.toOptionalModelScore,
      albumScore.toOptionalModelScore,
      artistScore.toOptionalModelScore,
    )
    songScore
      .map(makeScored(ScoreSource.Song))
      .orElse(albumScore.map(makeScored(ScoreSource.Album)))
      .orElse(artistScore.map(makeScored(ScoreSource.Artist)))
      .getOrElse(FullInfoScore.Default)
  }
}
