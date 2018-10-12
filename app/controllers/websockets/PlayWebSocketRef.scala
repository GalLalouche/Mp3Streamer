package controllers.websockets

import play.api.mvc.WebSocket
import rx.lang.scala.Observable

trait PlayWebSocketRef extends WebSocketRef {
  def broadcast(msg: String): Unit
  def closeConnections(): Unit
  def accept(): WebSocket

  def connections: Observable[Unit]
  def messages: Observable[String]
}
