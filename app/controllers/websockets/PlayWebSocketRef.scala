package controllers.websockets

import play.api.mvc.WebSocket

trait PlayWebSocketRef extends WebSocketRef {
  def accept(): WebSocket
}
