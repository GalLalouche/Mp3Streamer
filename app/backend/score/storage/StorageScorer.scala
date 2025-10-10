package backend.score.storage

import backend.score.{ModelScore, OptionalModelScore}

import scala.concurrent.{ExecutionContext, Future}

import cats.data.OptionT
import cats.syntax.functor.toFunctorOps

import common.storage.StorageTemplate

private[score] trait StorageScorer[A] { self: StorageTemplate[A, ModelScore] =>
  protected implicit def ec: ExecutionContext
  final def apply(a: A): OptionT[Future, ModelScore] = load(a)
  final def updateScore(a: A, score: OptionalModelScore): Future[Unit] = (score.toModelScore match {
    case None => delete(a)
    case Some(s) => replace(a, s)
  }).value.void
}
