package controllers.websockets

import play.api.mvc.WebSocket
import rx.lang.scala.Observable

trait PlayWebSocketRef extends WebSocketRef {
  def accept(): WebSocket
}
