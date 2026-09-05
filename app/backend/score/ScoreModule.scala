package backend.score

import backend.score.model.ModelScore
import backend.score.scorer.ScorerModule
import backend.score.storage.StorageModule
import net.codingwell.scalaguice.ScalaModule

object ScoreModule extends ScalaModule {
  override def configure(): Unit = {
    install(StorageModule)
    install(ScorerModule)
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
