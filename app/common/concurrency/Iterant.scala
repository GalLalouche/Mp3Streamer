package common.concurrency

import scala.concurrent.ExecutionContext
import scala.language.higherKinds
import scalaz.{Monad, OptionT, StreamT}
import scalaz.syntax.functor.ToFunctorOps
import scalaz.syntax.monad._

import common.rich.func.{RichOptionT, RichStreamT, TuplePLenses}
import common.rich.func.BetterFutureInstances._

/**
 * A non-sealed alternative to RichStreamT which is possible to extend using different
 * implementations.
 */
abstract class Iterant[F[_]: Monad, A] {
  def filter(p: A => Boolean): Iterant[F, A] = new Iterant[F, A] {
    override def step = Iterant.this.step.flatMap { case (head, tail) =>
      if (p(head)) OptionT.some(head -> tail.filter(p)) else tail.filter(p).step
    }
  }
  def oMap[B](f: A => Option[B]): Iterant[F, B] = map(f).filter(_.isDefined).map(_.get)
  def step: OptionT[F, (A, Iterant[F, A])]
  def map[B](f: A => B): Iterant[F, B] = new Iterant[F, B] {
    override def step = Iterant.this.step.map { case (head, tail) =>
      f(head) -> tail.map(f)
    }
  }
  def flatMapF[B](f: A => F[B]): Iterant[F, B] = new Iterant[F, B] {
    override def step = Iterant.this.step.flatMapF { case (head, tail) =>
      f(head).map(_ -> tail.flatMapF(f))
    }
  }
  def batchStep(
      n: Int,
      onNext: A => Unit = _ => (),
      onComplete: () => Unit = () => (),
  ): F[(Seq[A], Iterant[F, A])] = {
    lazy val default = (Seq.empty[A] -> this).point
    if (n == 0) default
    else
      step.run.flatMap {
        case None =>
          onComplete()
          default
        case Some((head, tail)) =>
          onNext(head)
          tail
            .batchStep(n - 1, onNext, onComplete)
            .map(TuplePLenses.tuple2First.modify(head :: _.toList))
      }
  }
  def toStream: StreamT[F, A] = StreamT(
    step.fold(e => StreamT.Yield(e._1, e._2.toStream), StreamT.Done),
  )
  def ++(other: => Iterant[F, A]): Iterant[F, A] = {
    lazy val lazyOther = other
    new Iterant[F, A] {
      override def step =
        Iterant.this.step.map(TuplePLenses.tuple2Second.modify(_ ++ other)).orElse(lazyOther.step)
    }
  }
}

object Iterant {
  def empty[F[_]: Monad, A]: Iterant[F, A] = fromStream(StreamT.empty)
  def range[F[_]: Monad](start: Int, until: Int, by: Int = 1): Iterant[F, Int] =
    new Iterant[F, Int] {
      override def step =
        if (start >= until)
          OptionT.none
        else
          RichOptionT.pointSome[F].apply(start -> range(start + by, until, by))
    }
  def from[F[_]: Monad](start: Int, by: Int = 1): Iterant[F, Int] = new Iterant[F, Int] {
    override def step = RichOptionT.pointSome[F].apply(start -> from(start + by, by))
  }

  def fromStream[F[_]: Monad, A]($ : StreamT[F, A]): Iterant[F, A] = new Iterant[F, A] {
    override def step = OptionT[F, (A, StreamT[F, A])]($.uncons).map { case (next, s) =>
      (next, fromStream(s))
    }
    override def toStream = $
  }

  def fromProducer[A]($ : AsyncProducer[A])(implicit ec: ExecutionContext): FutureIterant[A] =
    fromStream(RichStreamT.fillM(OptionT($.!())))

  def prefetching[F[_]: Monad, A]($ : Iterant[F, A], n: Int): Iterant[F, A] =
    PrefetchingIterant[F, A]($, n)
}
