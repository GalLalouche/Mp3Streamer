package common.concurrency

import cats.{Eval, Monad}
import cats.data.OptionT
import cats.implicits.catsSyntaxFunctorTuple2Ops
import common.rich.func.kats.ToMoreFunctorOps.toMoreFunctorOps

import common.rich.RichT.richT

/**
 * This has the benefit over using something like [[LinkedBlockingQueue]], since it doesn't take up
 * a thread blocking until the queue opens up!
 */
private class PrefetchingIterant[F[_]: Monad, A](
    private val head: OptionT[F, A],
    private val tail: Eval[OptionT[F, PrefetchingIterant[F, A]]],
    capacity: Int,
) extends Iterant[F, A] {
  // Note: this probably isn't stack safe, but for small capacities this shouldn't matter.
  private def forceEvaluation(n: Int): Unit = if (n > 0) tail.value.listen(_.forceEvaluation(n - 1))
  override lazy val step: Step[A] = for {
    h <- head
    t <- tail.value
  } yield {
    t.forceEvaluation(capacity)
    (h, t)
  }
}

private object PrefetchingIterant {
  def apply[F[_]: Monad, A](i: Iterant[F, A], capacity: Int): PrefetchingIterant[F, A] = {
    // Ensures that the prevaluation is reduced by 1 for each subsequent element in the tail, while the
    // capacity remains unchanged (which is needed for future prefetches after step).
    def preevaluating(i: Iterant[F, A], initial: Int): PrefetchingIterant[F, A] = {
      val (h, t) = i.step.unzip
      new PrefetchingIterant(
        h,
        Eval.later(t).map(_.map(preevaluating(_, initial - 1))),
        capacity,
      ).<|(_.forceEvaluation(initial - 1))
    }
    preevaluating(i, capacity)
  }
}
