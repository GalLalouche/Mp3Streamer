package backend.scorer

import backend.recon.{Album, Artist}
import backend.scorer.storage.{AlbumScoreStorage, ArtistScoreStorage, CompositeStorageScorer, SongScoreStorage, StorageScorer}
import com.google.inject.Provides
import models.{MusicFinder, Song}
import net.codingwell.scalaguice.ScalaModule

object ScorerModule extends ScalaModule {
  override def configure(): Unit = {
    bind[StorageScorer[Artist]].to[ArtistScoreStorage]
    bind[StorageScorer[Album]].to[AlbumScoreStorage]
    bind[StorageScorer[Song]].to[SongScoreStorage]
    bind[FullInfoModelScorer].to[CompositeStorageScorer]
    bind[ScoreBasedProbability].to[FlatScoreBasedProbability]
    bind[CachedModelScorer].to[CachedModelScorerState]
    bind[ModelScorer].to[ModelScorerImpl]
  }

  @Provides private def provideScoreBasedProbability(
      scorer: CachedModelScorer,
      mf: MusicFinder,
  ): FlatScoreBasedProbability = {
    def requiredProbability: ModelScore => Double = {
      case ModelScore.Crappy => 0
      case ModelScore.Meh => 0.02
      case ModelScore.Okay => 0.18
      case ModelScore.Good => 0.37
      case ModelScore.Great => 0.25
      case ModelScore.Amazing => 0.18
    }
    val defaultScore = requiredProbability(ModelScore.Okay)
    FlatScoreBasedProbability.withAsserts(
      requiredProbability,
      defaultScore,
      scorer,
      mf.getSongFiles,
    )
  }
}
