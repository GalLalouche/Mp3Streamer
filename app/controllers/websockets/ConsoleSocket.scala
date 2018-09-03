package controllers.websockets

import backend.logging.{Logger, StringOutputLogger}
import javax.inject.Inject

/** Sends console messages to the listeners */
class ConsoleSocket @Inject()(logger: Logger) extends WebSocketController(logger) with StringOutputLogger {
  override protected def output(what: String): Unit = broadcast(what)
}
