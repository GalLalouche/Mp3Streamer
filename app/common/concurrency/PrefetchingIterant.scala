package common.concurrency

import common.rich.func.ToMoreFunctorOps.toMoreFunctorOps
import scalaz.{Monad, Need, OptionT}
import scalaz.Scalaz.ToFunctorOps

import common.rich.RichT.richT

private class PrefetchingIterant[F[_]: Monad, A](
    private val head: OptionT[F, A],
    private val tail: Need[OptionT[F, PrefetchingIterant[F, A]]],
    capacity: Int,
) extends Iterant[F, A] {
  private def forceEvaluation(n: Int): Unit = if (n > 0)
    tail.value.listen(_.forceEvaluation(n - 1))
  override def step = for {
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
        Need(t).map(_.map(preevaluating(_, initial - 1))),
        capacity,
      ).<|(_.forceEvaluation(initial - 1))
    }
    preevaluating(i, capacity)
  }
}
