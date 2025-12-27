package common.concurrency

import scala.concurrent.{Await, Promise}
import scala.concurrent.duration.Duration

/** Simple utility when you just need a latch of size 1. */
class SingleLatch private (private val promise: Promise[Unit]) extends AnyVal {
  def await(duration: Duration = Duration.Inf): Unit = Await.result(promise.future, duration)
  def release(): Unit = promise.success(())
  /**
   * This takes a [[RuntimeException]] since [[InterruptedException]], [[ControlException]], and
   * [[Error]] have special meaning in promise Scala concurrency (which God forbid they bother to
   * document), so forcing a [[RuntimeException]] avoids these issues.
   */
  def interrupt(e: RuntimeException): Unit = promise.failure(e)
}

object SingleLatch {
  def apply(): SingleLatch = new SingleLatch(Promise())
}
