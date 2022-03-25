package backend.scorer.storage

import backend.scorer.ModelScore
import backend.FutureOption

import scala.concurrent.Future

private[scorer] trait StorageScorer[A] {
  def apply(a: A): FutureOption[ModelScore]
  def updateScore(a: A, score: ModelScore): Future[Unit]
}
