package controllers.websockets

import javax.inject.Inject

import play.api.mvc.{InjectedController, WebSocket}

/** Sends console messages to the listeners. */
// TODO this should be configured as a handler!
class ConsoleSocket @Inject() (webSocketFactory: PlayWebSocketRegistryFactory)
    extends InjectedController {
  private val webSocket = webSocketFactory("Console")
  // protected override def output(what: String): Unit = webSocket.broadcast(what)
  def accept(): WebSocket = webSocket.accept()
}
