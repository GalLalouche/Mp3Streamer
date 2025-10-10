package common.concurrency

import scala.collection.LinearSeq

import cats.Monad
import cats.data.OptionT
import cats.implicits.catsSyntaxEitherId
import cats.syntax.flatMap._
import cats.syntax.functor.toFunctorOps
import common.rich.func.TuplePLenses.{__1, __2}
import common.rich.func.kats.ToMoreFoldableOps.toMoreFoldableOps
import common.rich.func.kats.Transable

import common.concurrency.Iterant.TakeIterant
import common.rich.collections.RichLinearSeq.richLinearSeq

/**
 * A non-sealed alternative to StreamT which can be extended using different implementations.
 *
 * In general, this can be thought of as the effectful equivalent of an [[Iterable]], where the
 * interface is the simpler (and not necessarily mutable)` next: Option[(A, Iterable[A])]` instead
 * of the `hasNext` and `next` pair. Since the computation is effectful, this becomes: `F[Option[(A,
 * Iterant[F, A])]]`, or using transformers: `OptionT[F, (A, Iterant[F, A)]`.
 */
// TODO replace with fs2 streams?
abstract class Iterant[F[_]: Monad, A] {
  final type Step[X] = OptionT[F, (X, Iterant[F, X])]
  /**
   * The main method of this class: returns the head and tail, if it exists. For the most part, this
   * should be overriden with `lazy val`, since not all F's are properly behaving, e.g.,
   * [[scala.concurrent.Future]].
   */
  def step: Step[A]

  def batchStep(
      n: Int,
      onNext: A => Unit = _ => (),
      onComplete: () => Unit = () => (),
  ): F[(Seq[A], Iterant[F, A])] = {
    lazy val default = Monad[F].pure(Seq.empty[A] -> this)
    if (n == 0)
      default
    else
      step.value.flatMap {
        case None =>
          onComplete()
          default
        case Some((head, tail)) =>
          onNext(head)
          tail
            .batchStep(n - 1, onNext, onComplete)
            .map(__1.modify(head :: _.toList))
      }
  }

  def oMap[B](f: A => Option[B]): Iterant[F, B] = map(f).filter(_.isDefined).map(_.get)
  def map[B](f: A => B): Iterant[F, B] = new Iterant[F, B] {
    override lazy val step: Step[B] = Iterant.this.step.map { case (head, tail) =>
      f(head) -> tail.map(f)
    }
  }
  def mapF[B](f: A => F[B]): Iterant[F, B] = new Iterant[F, B] {
    override lazy val step: Step[B] = Iterant.this.step.semiflatMap { case (head, tail) =>
      f(head).map(_ -> tail.mapF(f))
    }
  }

  def filter(p: A => Boolean): Iterant[F, A] = new Iterant[F, A] {
    override lazy val step: Step[A] = Iterant.this.step.flatMap { case (head, tail) =>
      if (p(head)) OptionT.some(head -> tail.filter(p)) else tail.filter(p).step
    }
  }
  def flatMap[B](f: A => Iterant[F, B]): Iterant[F, B] = new Iterant[F, B] {
    override lazy val step: Step[B] = Iterant.this.step.flatMap { case (head, tail) =>
      lazy val fTail = tail.flatMap(f)
      f(head).step
        .map[(B, Iterant[F, B])](__2.modify(_ ++ fTail))
        .orElse(fTail.step)
    }
  }
  def take(n: Int): Iterant[F, A] = new TakeIterant(n, this)

  /** Forces the evaluation under F. */
  def toSeq: F[Seq[A]] = Monad[F]
    .tailRecM[(Step[A], List[A]), List[A]]((step, Nil)) { case (s, l) =>
      s.value.map(o => o.mapHeadOrElse({ case (h, t) => (t.step, h :: l).asLeft }, l.asRight))
    }
    .map(_.toVector.reverse)

  def ++(other: => Iterant[F, A]): Iterant[F, A] = {
    lazy val other_ = other
    new Iterant[F, A] {
      override lazy val step: Step[A] =
        Iterant.this.step.map(__2.modify(_ ++ other_)).orElse(other_.step)
    }
  }
}

object Iterant {
  def empty[F[_]: Monad, A]: Iterant[F, A] = new Iterant[F, A] {
    override val step: Step[A] = OptionT.none
  }

  def range[F[_]: Monad](start: Int, until: Int, by: Int = 1): Iterant[F, Int] =
    new Iterant[F, Int] {
      override lazy val step: Step[Int] =
        if (start >= until)
          OptionT.none
        else
          Transable.OptionTransable.pureLift(start -> range(start + by, until, by))
    }

  def from[F[_]: Monad](start: Int, by: Int = 1): Iterant[F, Int] = new Iterant[F, Int] {
    override lazy val step: Step[Int] =
      Transable.OptionTransable.pureLift(start -> from(start + by, by))
  }
  def from[F[_]: Monad, A](fll: F[LinearSeq[A]]): Iterant[F, A] = new Iterant[F, A] {
    override lazy val step: Step[A] =
      OptionT(fll.map(new LinearSeqImpl[F, A](_)).flatMap(_.step.value))
  }

  /** Will force the evaluation of up to `n` elements; useful as a buffer. */
  def prefetching[F[_]: Monad, A]($ : Iterant[F, A], n: Int): Iterant[F, A] =
    PrefetchingIterant[F, A]($, n)
  def forever[F[_]: Monad, A](f: => F[A]): Iterant[F, A] = new Iterant[F, A] {
    override lazy val step: Step[A] = OptionT.liftF(f.tupleRight(forever(f)))
  }
  def unfold[F[_]: Monad, A](f: => OptionT[F, A]): Iterant[F, A] = new Iterant[F, A] {
    override lazy val step: Step[A] = f.tupleRight(unfold(f))
  }

  private class LinearSeqImpl[F[_]: Monad, A]($ : LinearSeq[A]) extends Iterant[F, A] {
    override lazy val step: Step[A] =
      OptionT.fromOption(
        $.headTailOption.map(__2.modify(new LinearSeqImpl(_))),
      )
  }

  private class TakeIterant[F[_]: Monad, A](n: Int, $ : Iterant[F, A]) extends Iterant[F, A] {
    override lazy val step: Step[A] =
      if (n == 0) OptionT.none
      else $.step.map(__2.modify(new TakeIterant(n - 1, _)))
  }
}
