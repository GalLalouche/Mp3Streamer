package backend.scorer

import javax.inject.Inject

import models.Song

import scala.concurrent.ExecutionContext

private class ModelScorerImpl @Inject() (
    fullInfoModelScorer: FullInfoModelScorer,
    ec: ExecutionContext,
) extends ModelScorer {
  private implicit val iec: ExecutionContext = ec
  override def apply(s: Song) = fullInfoModelScorer.apply(s).map(_.toOptionalModelScore)
}
