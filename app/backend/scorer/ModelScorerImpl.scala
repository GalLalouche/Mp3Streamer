package backend.scorer

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scalaz.OptionT

import models.Song

private class ModelScorerImpl @Inject() (
    fullInfoModelScorer: FullInfoModelScorer,
    ec: ExecutionContext,
) extends ModelScorer {
  private implicit val iec: ExecutionContext = ec
  override def apply(s: Song) = OptionT(fullInfoModelScorer.apply(s).map(_.toModelScore))
}
