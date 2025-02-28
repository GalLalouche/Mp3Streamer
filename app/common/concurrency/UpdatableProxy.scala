package common.concurrency

import scala.concurrent.Future

import common.TimedLogger
import common.concurrency.UpdatableProxy.Update

/** Can update itself asynchronously while still serving the old value. */
class UpdatableProxy[A] private[concurrency] (
    private var state: A,
    updateSelf: () => A,
    name: String,
    timedLogger: TimedLogger,
) {
  def update(): Future[Unit] = actor ! Update.FromFunction
  // To avoid data races, we still go through the actor.
  def set(a: A): Future[Unit] = actor ! Update.FromValue(a)
  def current: A = state

  private lazy val actor = SimpleActor[Update[A]](
    name + " Updatable",
    {
      case Update.FromFunction =>
        timedLogger("Updating " + name, scribe.debug(_)) { state = updateSelf() }
      case Update.FromValue(a) =>
        scribe.debug(s"Updating $name manually")
        state = a
    },
  )
}

private object UpdatableProxy {
  private sealed trait Update[+A]
  private object Update {
    case object FromFunction extends Update[Nothing]
    case class FromValue[A](a: A) extends Update[A]
  }
}
