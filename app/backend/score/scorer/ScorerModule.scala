package backend.score.scorer

import net.codingwell.scalaguice.ScalaModule

private[score] object ScorerModule extends ScalaModule {
  override def configure(): Unit = {
    bind[AggregateScorer].to[CachedModelScorerState]
    bind[IndividualScorer].to[CachedModelScorerState]
    bind[ScoreModel].to[ScoreModelImpl]
  }
}
