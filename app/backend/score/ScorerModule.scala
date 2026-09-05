package backend.score

import backend.recon.{Album, Artist, Track}
import backend.score.model.ModelScore
import backend.score.storage.{AlbumScoreStorage, ArtistScoreStorage, StorageScorer, TrackScoreStorage}
import net.codingwell.scalaguice.ScalaModule

object ScorerModule extends ScalaModule {
  override def configure(): Unit = {
    bind[StorageScorer[Artist]].to[ArtistScoreStorage]
    bind[StorageScorer[Album]].to[AlbumScoreStorage]
    bind[StorageScorer[Track]].to[TrackScoreStorage]
    bind[AggregateScorer].to[CachedModelScorerState]
    bind[IndividualScorer].to[CachedModelScorerState]
    bind[FullInfoScorer].to[CachedModelScorerState]
    bind[FullInfoModelScorer].to[ScorerModel]
    bind[Map[ModelScore, Any]].toInstance(RequiredProbability)
  }

  private val RequiredProbability: Map[ModelScore, Double] = Map(
    ModelScore.Crappy -> 0,
    ModelScore.Meh -> 0.02,
    ModelScore.Okay -> 0.18,
    ModelScore.Good -> 0.37,
    ModelScore.Great -> 0.25,
    ModelScore.Amazing -> 0.18,
  )
}
