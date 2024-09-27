package controllers.websockets

import akka.actor.{Actor, ActorRef, ActorSystem, PoisonPill, Props}
import akka.stream.ActorMaterializer
import play.api.libs.streams.ActorFlow
import play.api.mvc.WebSocket
import rx.lang.scala.Subject

import scala.collection.mutable

import common.rich.collections.RichSet

private object WebSocketRegistryImpl {
  private case class MessageToClient(str: String) extends AnyVal
  private implicit val system: ActorSystem = ActorSystem("WebSockets")
  private implicit val materializer: ActorMaterializer = ActorMaterializer()(system)
}

private class WebSocketRegistryImpl(name: String) extends PlayWebSocketRef {
  import controllers.websockets.WebSocketRegistryImpl._

  private val actors: mutable.Set[ActorRef] = RichSet.concurrentSet
  private class SocketActor(out: ActorRef) extends Actor {
    override def preStart() = actors += this.self
    override def postStop() = actors -= this.self
    def receive = {
      case msg: String =>
        scribe.trace(s"$name received message <$msg>")
        messagesSubject.onNext(msg)
      case MessageToClient(msg) => out ! msg
    }
  }
  private val connectionsSubject = Subject[Unit]()
  private val messagesSubject = Subject[String]()

  override def broadcast(msg: String) = actors.foreach(_ ! MessageToClient(msg))
  override def closeConnections() = actors.foreach(_ ! PoisonPill)
  override def accept() = WebSocket.accept[String, String] { _ =>
    // config.scribe.trace(s"${this.simpleName} received a new connection")
    connectionsSubject.onNext(())
    ActorFlow.actorRef(out => Props(new SocketActor(out)))
  }

  override val connections = connectionsSubject
  override val messages = messagesSubject
}
