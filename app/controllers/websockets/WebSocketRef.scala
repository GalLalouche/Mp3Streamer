package controllers.websockets

import scala.concurrent.Future
import scalaz.Reader

import rx.lang.scala.Observable

trait WebSocketRef {
  def broadcast(msg: String): Unit
  def closeConnections(): Unit

  def connections: Observable[Unit]
  def messages: Observable[String]
}

object WebSocketRef {
  type WebSocketRefReader = Reader[WebSocketRef, Unit]
  type AsyncWebSocketRefReader = Reader[WebSocketRef, Future[Unit]]
}
