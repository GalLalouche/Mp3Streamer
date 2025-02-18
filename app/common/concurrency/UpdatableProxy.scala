package common.concurrency

import enumeratum.{Enum, EnumEntry}

import scala.collection.immutable
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
  private case class FromValue(a: A) extends Update
  def update(): Future[Unit] = extra ! Update.FromFunction
  // To avoid data races, we still go through the actor.
  def set(a: A): Future[Unit] = extra ! FromValue(a)
  def current: A = state

  private lazy val extra = SimpleActor[Update](
    name + " Updatable",
    {
      case Update.FromFunction =>
        timedLogger.apply("Updating " + name, scribe.debug(_)) {
          state = updateSelf()
        }
      case FromValue(a) =>
        scribe.debug(s"Updating $name manually")
        state = a
    },
  )
}

private object UpdatableProxy {
  private sealed trait Update extends EnumEntry
  private object Update extends Enum[Update] {
    case object FromFunction extends Update
    override def values: immutable.IndexedSeq[Update] = findValues
  }
}
