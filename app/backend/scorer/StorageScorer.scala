package backend.scorer

import backend.FutureOption

import scala.concurrent.Future

private trait StorageScorer[A] {
  def apply(a: A): FutureOption[ModelScore]
  def store(a: A, score: ModelScore): Future[Unit]
}
