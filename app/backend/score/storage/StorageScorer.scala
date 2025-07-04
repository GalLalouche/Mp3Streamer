package backend.score.storage

import backend.score.{ModelScore, OptionalModelScore}

import scala.concurrent.{ExecutionContext, Future}

import common.rich.func.BetterFutureInstances._
import scalaz.OptionT
import scalaz.Scalaz.ToFunctorOps

import common.storage.StorageTemplate

private[score] trait StorageScorer[A] { self: StorageTemplate[A, ModelScore] =>
  protected implicit def ec: ExecutionContext
  final def apply(a: A): OptionT[Future, ModelScore] = load(a)
  final def updateScore(a: A, score: OptionalModelScore): Future[Unit] = (score.toModelScore match {
    case None => delete(a)
    case Some(s) => replace(a, s)
  }).run.void
}
