package common.concurrency

import java.util.concurrent.atomic.{AtomicInteger, AtomicLong}
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.Duration

import backend.logging.Logger
import common.rich.RichFuture

private class RateLimitedActorAsyncImpl[Msg, Result](
    name: String,
    f: Msg => Future[Result],
    rateLimit: Duration,
    logger: Logger,
) extends SimpleTypedActor[Msg, Result] {
  private val ec: ExecutionContext = SingleThreadedJobQueue.executionContext(name)
  private val lastRun = new AtomicLong(0)
  private val i = new AtomicInteger(0)
  def !(m: => Msg): Future[Result] = RichFuture.fromCallback(c =>
    ec.execute { () =>
      val now = System.currentTimeMillis()
      val index = i.incrementAndGet()
      logger.verbose(s"<$index>: <$now>")
      val l = lastRun.getAndSet(now)
      val sleepTime = l - now + rateLimit.toMillis
      if (sleepTime >= 0) {
        logger.verbose(s"<$i>: Now: <$now>, last run time: <$l>, sleeping for <$sleepTime>")
        Thread.sleep(sleepTime)
      }
      logger.verbose(s"<$i> start @ ${System.currentTimeMillis()}")
      lastRun.set(System.currentTimeMillis())
      val $ = Await.result(f(m), Duration.Inf)
      logger.verbose(s"<$i> done  @ ${System.currentTimeMillis()}")
      c($)
    },
  )
}
