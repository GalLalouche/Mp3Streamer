package backend.scorer

import backend.FutureOption

import scala.concurrent.Future

trait StorageScorer[A] {
  def apply(a: A): FutureOption[ModelScore]
  def updateScore(a: A, score: ModelScore): Future[Unit]
}
