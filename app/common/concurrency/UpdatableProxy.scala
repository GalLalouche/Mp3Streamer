package common.concurrency

import java.util.concurrent.atomic.AtomicReference

import scribe.Level

import scala.concurrent.Future

import common.TimedLogger
import common.concurrency.UpdatableProxy.Update
import common.rich.RichT.richT

/**
 * Can update itself asynchronously while still serving the old value. This differs from
 * [[common.json.saver.JsonableCOW]] since obviously the proxy does not (necessarily) copies itself
 * on updates, but also in that the proxy can update itself, and that it is assumed this is a long
 * process (hence the use of [[Future]] all around).
 */
class UpdatableProxy[A] private[concurrency] (
    private val state: AtomicReference[A],
    updateSelf: () => A,
    name: String,
    timedLogger: TimedLogger,
) extends ActorState[A, A] {
  def update(): Future[A] = actor ! Update.FromFunction
  // To avoid data races, we still go through the actor.
  override def set(a: A): Future[A] = actor ! Update.FromValue(a)
  override def get: A = state.get()

  private lazy val actor = SimpleTypedActor[Update[A], A](
    name + " Updatable",
    {
      case Update.FromFunction =>
        timedLogger("Updating " + name, Level.Debug)(updateSelf() <| state.set)
      case Update.FromValue(a) =>
        scribe.debug(s"Updating $name manually")
        a <| state.set
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
