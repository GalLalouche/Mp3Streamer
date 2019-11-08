package controllers.websockets

import backend.logging.StringOutputLogger
import javax.inject.Inject
import play.api.mvc.{InjectedController, WebSocket}

/** Sends console messages to the listeners. */
class ConsoleSocket @Inject()(webSocketFactory: PlayWebSocketRegistryFactory) extends InjectedController
    with StringOutputLogger {
  private val webSocket = webSocketFactory("Console")
  override protected def output(what: String): Unit = webSocket.broadcast(what)
  def accept(): WebSocket = webSocket.accept()
}
