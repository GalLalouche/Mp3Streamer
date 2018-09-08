package controllers.websockets

import java.util.concurrent.ConcurrentHashMap
import java.util.Collections

import akka.actor.{Actor, ActorRef, ActorSystem, PoisonPill, Props}
import akka.stream.ActorMaterializer
import backend.logging.Logger
import play.api.libs.streams.ActorFlow
import play.api.mvc.WebSocket
import rx.lang.scala.Subject

import scala.collection.JavaConverters._
import scala.collection.mutable

private object WebSocketRegistryImpl {
  private case class MessageToClient(str: String) extends AnyVal
  private implicit val system: ActorSystem = ActorSystem("WebSockets")
  private implicit val materializer: ActorMaterializer = ActorMaterializer()(system)
}

private class WebSocketRegistryImpl(logger: Logger, name: String) extends WebSocketRegistry {
  import controllers.websockets.WebSocketRegistryImpl._

  // TODO extract this newSet BS to a helper class.
  private val actors: mutable.Set[ActorRef] =
    Collections.newSetFromMap[ActorRef](new ConcurrentHashMap[ActorRef, java.lang.Boolean]()).asScala
  private class SocketActor(out: ActorRef) extends Actor {
    override def preStart() = actors += this.self
    override def postStop() = actors -= this.self
    def receive = {
      case msg: String =>
        logger.verbose(s"$name received message <$msg>")
        messagesSubject.onNext(msg)
      case MessageToClient(msg) => out ! msg
    }
  }
  private val connectionsSubject = Subject[Unit]()
  private val messagesSubject = Subject[String]()

  override def broadcast(msg: String) = actors.foreach(_ ! MessageToClient(msg))
  override def closeConnections() = actors.foreach(_ ! PoisonPill)
  override def accept = WebSocket.accept[String, String] {_ =>
    //config.logger.verbose(s"${this.simpleName} received a new connection")
    connectionsSubject.onNext(())
    ActorFlow.actorRef(out => Props(new SocketActor(out)))
  }

  override val connections = connectionsSubject
  override val messages = messagesSubject
}
