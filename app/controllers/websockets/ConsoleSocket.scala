package controllers.websockets

import javax.inject.Inject

import backend.logging.StringOutputLogger
import play.api.mvc.{InjectedController, WebSocket}

/** Sends console messages to the listeners. */
class ConsoleSocket @Inject() (webSocketFactory: PlayWebSocketRegistryFactory)
    extends InjectedController
    with StringOutputLogger {
  private val webSocket = webSocketFactory("Console")
  protected override def output(what: String): Unit = webSocket.broadcast(what)
  def accept(): WebSocket = webSocket.accept()
}
