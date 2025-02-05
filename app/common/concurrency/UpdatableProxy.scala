package common.concurrency

import scala.concurrent.Future

import common.TimedLogger

/** Can update itself asynchronously while still serving the old value. */
class UpdatableProxy[A] private[concurrency] (
    private var state: A,
    updateSelf: () => A,
    name: String,
    timedLogger: TimedLogger,
) {
  def update(): Future[Unit] = extra.!()
  def current: A = state

  private lazy val extra = Extra(name + " Updatable") {
    timedLogger.apply("Updating " + name, scribe.debug(_)) {
      state = updateSelf()
    }
  }
}
