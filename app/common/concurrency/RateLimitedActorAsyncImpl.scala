package common.concurrency

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.Duration

private class RateLimitedActorAsyncImpl[Msg, Result](
    name: String,
    f: Msg => Future[Result],
    rateLimit: Duration,
) extends SimpleTypedActor[Msg, Result] {
  private implicit val ec: ExecutionContext = DaemonExecutionContext.single(name)
  // No need to synchronize these since we're on a single thread.
  private var lastRun = 0L
  private var i = 0
  def !(m: => Msg): Future[Result] = Future {
    val now = System.currentTimeMillis()
    scribe.trace(s"<$i>: <$lastRun>")
    i += 1
    val sleepTime = lastRun - now + rateLimit.toMillis
    if (sleepTime > 0) {
      scribe.trace(s"<$i>: Now: <$now>, last run time: <$lastRun>, sleeping for <$sleepTime>")
      Thread.sleep(sleepTime)
    }
    scribe.trace(s"<$i> start @ ${System.currentTimeMillis()}")
    // We use Await because this has to be a single Future task to ensure rate limiting.
    val $ = Await.result(f(m), Duration.Inf)
    lastRun = System.currentTimeMillis()
    scribe.trace(s"<$i> done  @ ${System.currentTimeMillis()}")
    $
  }
}
