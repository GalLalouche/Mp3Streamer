package backend.scorer.storage

import backend.scorer.{ModelScore, OptionalModelScore}
import backend.FutureOption

import scala.concurrent.ExecutionContext

import scalaz.Scalaz.ToFunctorOps
import common.rich.func.BetterFutureInstances._

import common.storage.StorageTemplate

private[scorer] trait StorageScorer[A] { self: StorageTemplate[A, ModelScore] =>
  def apply(a: A): FutureOption[ModelScore]
  protected implicit def ec: ExecutionContext
  final def updateScore(a: A, score: OptionalModelScore) = (score.toModelScore match {
    case None => delete(a)
    case Some(s) => replace(a, s)
  }).run.void
}
