package common.concurrency

import backend.logging.LoggingLevel

import scala.concurrent.Future

import common.TimedLogger

class UpdatableProxy[A](
    private var state: A,
    updateSelf: () => A,
    name: String,
    timedLogger: TimedLogger,
) {
  private val extra = Extra(name + " Updatable") {
    timedLogger.apply("Updating " + name, LoggingLevel.Debug) {
      state = updateSelf()
    }
  }
  def update(): Future[Unit] = extra.!()
  def current: A = state
}
