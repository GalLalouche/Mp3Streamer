package backend.scorer.storage

import backend.scorer.{ModelScore, OptionalModelScore}

import scala.concurrent.ExecutionContext

import common.rich.func.BetterFutureInstances._
import scalaz.Scalaz.ToFunctorOps

import common.storage.StorageTemplate

private[scorer] trait StorageScorer[A] { self: StorageTemplate[A, ModelScore] =>
  protected implicit def ec: ExecutionContext
  final def apply(a: A) = load(a)
  final def updateScore(a: A, score: OptionalModelScore) = (score.toModelScore match {
    case None => delete(a)
    case Some(s) => replace(a, s)
  }).run.void
}
