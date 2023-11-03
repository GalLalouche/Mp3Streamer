package backend.scorer.storage

import scala.concurrent.Future

import backend.scorer.ModelScore
import backend.FutureOption

private[scorer] trait StorageScorer[A] {
  def apply(a: A): FutureOption[ModelScore]
  def updateScore(a: A, score: ModelScore): Future[Unit]
}
