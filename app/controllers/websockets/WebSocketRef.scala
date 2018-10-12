package controllers.websockets

import rx.lang.scala.Observable

import scalaz.Reader

trait WebSocketRef {
  def broadcast(msg: String): Unit
  def closeConnections(): Unit

  def connections: Observable[Unit]
  def messages: Observable[String]
}

object WebSocketRef {
  type WebSocketRefReader = Reader[WebSocketRef, Unit]
}
