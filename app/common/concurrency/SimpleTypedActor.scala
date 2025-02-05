package common.concurrency

import scala.concurrent.Future
import scala.concurrent.duration.Duration

/** It's a single threaded future factory, basically. */
trait SimpleTypedActor[Msg, +Result] {
  def !(m: => Msg): Future[Result]
}

object SimpleTypedActor {
  def apply[Msg, Result](name: String, f: Msg => Result): SimpleTypedActor[Msg, Result] =
    new SimpleTypedActorImpl(name, f)

  def async[Msg, Result](name: String, f: Msg => Future[Result]): SimpleTypedActor[Msg, Result] =
    new SimpleTypedActorAsyncImpl(name, f)

  /** Ensures at least [rateLimit] time has passed between handling messages. */
  def asyncRateLimited[Msg, Result](
      name: String,
      f: Msg => Future[Result],
      rateLimit: Duration,
  ): SimpleTypedActor[Msg, Result] = new RateLimitedActorAsyncImpl(name, f, rateLimit)

  /**
   * Ensures uniqueness of the messages in the message queue, i.e., if a sent message already exists
   * in the queue it will be dropped. This can be used to avoid doing unnecessary work.
   */
  def unique[Msg, Result](
      name: String,
      f: Msg => Result,
  ): SimpleTypedActor[Msg, Result] = new UniqueSimpleTypedActorImpl(name, f)
}
