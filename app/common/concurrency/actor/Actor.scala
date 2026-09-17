package common.concurrency.actor

import scala.concurrent.Future
import scala.concurrent.duration.{Duration, FiniteDuration}

import common.concurrency.actor.ActorBuilder.ActorConstructor

/**
 * It's a single threaded future factory, basically. A note on thread-safety guarantees: while it is
 * *not* guaranteed that all messages will be processed by the *same* thread, it *is* guaranteed
 * that they are processed one at a time, i.e., no two messages will be processed concurrently. It
 * is also not required to synchronize or otherwise safely publish whatever data *only* the actor
 * sees, since the execution of different tasks has a happens-before relationship (any data seen by
 * other threads needs to be safely published, of course). Finally, it is also guaranteed that
 * messages are processed in the order they are sent, if these messages share a happens-before link.
 */
trait Actor[-Msg, +Result] {
  def !(m: => Msg): Future[Result]
  def !()(implicit ev: Unit <:< Msg): Future[Result] = this.!(ev(()))
  def name: String
}

object Actor {
  def apply[Msg, Result](name: String): ActorBuilder[Msg, Result] =
    ActorBuilder.builder[Msg, Result](new ActorConstructor {
      override def apply[M, R](f: ActorFunction[M, R]): Actor[M, R] =
        new ActorImpl[M, R](name, f)
    })

  /**
   * Ensures at least [rateLimit] time has passed between handling messages (that is, between the
   * application of the function to the message, not between the end of one Future to the start of
   * another). Rate limiting is achieved by waiting between messages, not dropping.
   */
  def rateLimited[Msg, Result](name: String, rateLimit: Duration): ActorBuilder[Msg, Result] =
    ActorBuilder.builder[Msg, Result](new ActorConstructor {
      override def apply[M, R](f: ActorFunction[M, R]): Actor[M, R] =
        new RateLimitedActorImpl[M, R](name, f, rateLimit)
    })

  /**
   * Ensures uniqueness of the messages in the message queue, i.e., if a sent message already exists
   * in the queue, it will be dropped. This can be used to avoid doing unnecessary work.
   */
  def unique[Msg, Result](name: String): ActorBuilder[Msg, Result] =
    ActorBuilder.builder[Msg, Result](new ActorConstructor {
      override def apply[M, R](f: ActorFunction[M, R]): Actor[M, R] =
        new UniqueActorImpl[M, R](name, f)
    })

  /*
   * Combines rate limit (see above) with exponential backoff on actor failure, up to `maxRetries`
   * times. Will sleep `2^attemptNumber * rateLimit` between retries (After the first failure, we
   * are at attempt number 1).
   * N.B. Backing off involves sleeping on (this) actor's thread, so multiple backoffs will block
   * each other.
   */
  def exponentialBackoff[Msg, Result](
      name: String,
      maxRetries: Int,
      rateLimit: FiniteDuration,
      errorListener: Throwable => Unit = _ => (),
  ): ActorBuilder[Msg, Result] =
    ActorBuilder.builder[Msg, Result](new ActorConstructor {
      override def apply[M, R](f: ActorFunction[M, R]): Actor[M, R] = {
        val rateLimited = new RateLimitedActorImpl[M, R](name, f, rateLimit)
        new ExponentialBackoffer[M, R](rateLimited, maxRetries, rateLimit, errorListener)
      }
    })
}
