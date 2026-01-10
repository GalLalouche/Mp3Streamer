package backend.score.storage

import backend.score.{ModelScore, OptionalModelScore}

import scala.concurrent.{ExecutionContext, Future}

import cats.data.OptionT
import cats.implicits.{catsSyntaxApplyOps, toFoldableOps}
import cats.syntax.functor.toFunctorOps
import common.rich.func.kats.PlainSeqInstances.plainSeqInstances

import common.storage.StorageTemplate

private[score] trait StorageScorer[A] { self: StorageTemplate[A, ModelScore] =>
  protected implicit def ec: ExecutionContext
  final def apply(a: A): OptionT[Future, ModelScore] = load(a)
  final def updateScore(a: A, score: OptionalModelScore): Future[Unit] = (score.toModelScore match {
    case None => delete(a)
    case Some(s) => replace(a, s)
  }).value.void
  def updateAll(xs: collection.Seq[(A, OptionalModelScore)]): Future[Unit] = {
    val (deletes, replacements) = xs.partitionEither(x =>
      x._2.toModelScore match {
        case None => Left(x._1)
        case Some(s) => Right((x._1, s))
      },
    )

    self.deleteAll(deletes) *> self.overwriteMultipleVoid(replacements)
  }
}
