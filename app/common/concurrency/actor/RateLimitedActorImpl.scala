package common.concurrency.actor

import scala.concurrent.Future
import scala.concurrent.duration.Duration

import common.rich.func.kats.PureError

private class RateLimitedActorImpl[Msg, Result](
    name: String,
    f: ActorFunction[Msg, Result],
    rateLimit: Duration,
) extends ActorTemplate[Msg, Result](name) {
  // No need to synchronize these since we're on a single thread.
  private var lastRun = 0L
  private var i = 0
  def !(m: => Msg): Future[Result] = Future.delegate {
    synchronized {
      val now = System.currentTimeMillis()
      scribe.trace(s"<$i>: <$lastRun>")
      i += 1
      val sleepTime = lastRun - now + rateLimit.toMillis
      if (sleepTime > 0) {
        scribe.trace(s"<$i>: Now: <$now>, last run time: <$lastRun>, sleeping for <$sleepTime>")
        Thread.sleep(sleepTime)
      }
      scribe.trace(s"<$i> start @ ${System.currentTimeMillis()}")
      val $ = PureError.tryWrap(f(this, m))
      lastRun = System.currentTimeMillis()
      $
    }
  }
}
