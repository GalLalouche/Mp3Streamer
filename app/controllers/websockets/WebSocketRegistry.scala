package controllers.websockets

import play.api.mvc.WebSocket
import rx.lang.scala.Observable

trait WebSocketRegistry {
  def broadcast(msg: String): Unit
  def closeConnections(): Unit
  def accept(): WebSocket

  def connections: Observable[Unit]
  def messages: Observable[String]
}
