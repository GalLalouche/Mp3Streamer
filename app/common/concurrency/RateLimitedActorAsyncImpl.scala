package common.concurrency

import java.util.concurrent.atomic.{AtomicInteger, AtomicLong}

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.Duration

import common.rich.RichFuture

private class RateLimitedActorAsyncImpl[Msg, Result](
    name: String,
    f: Msg => Future[Result],
    rateLimit: Duration,
) extends SimpleTypedActor[Msg, Result] {
  private val ec: ExecutionContext = DaemonExecutionContext.single(name)
  private val lastRun = new AtomicLong(0)
  private val i = new AtomicInteger(0)
  def !(m: => Msg): Future[Result] = RichFuture.fromCallback(callback =>
    ec.execute { () =>
      val now = System.currentTimeMillis()
      val index = i.incrementAndGet()
      scribe.trace(s"<$index>: <$now>")
      val l = lastRun.getAndSet(now)
      val sleepTime = l - now + rateLimit.toMillis
      if (sleepTime > 0) {
        scribe.trace(s"<$i>: Now: <$now>, last run time: <$l>, sleeping for <$sleepTime>")
        Thread.sleep(sleepTime)
      }
      scribe.trace(s"<$i> start @ ${System.currentTimeMillis()}")
      val $ = Await.result(f(m), Duration.Inf)
      lastRun.set(System.currentTimeMillis())
      scribe.trace(s"<$i> done  @ ${System.currentTimeMillis()}")
      callback($)
    },
  )
}
