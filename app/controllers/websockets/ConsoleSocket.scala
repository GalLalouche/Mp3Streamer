package controllers.websockets

import backend.logging.StringOutputLogger


/** Sends console messages to the listeners */
object ConsoleSocket extends WebSocketController with StringOutputLogger {
  override protected def output(what: String): Unit = safePush(what)
}
