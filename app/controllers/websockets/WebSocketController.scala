package controllers.websockets

import akka.actor.{Actor, ActorRef, ActorSystem, PoisonPill, Props}
import akka.stream.ActorMaterializer
import common.rich.RichT._
import controllers.{ControllerUtils, LegacyController}
import play.api.libs.streams.ActorFlow
import play.api.mvc.WebSocket

import scala.collection.mutable

// This has to appear before the trait, otherwise materializer won't be available as an implicit val?!
object WebSocketController {
  private case class MessageToClient(str: String) extends AnyVal
  private implicit val system = ActorSystem("WebSockets")
  private implicit val materializer = ActorMaterializer()(system)
}

trait WebSocketController extends LegacyController {
  import ControllerUtils.config
  import WebSocketController._
  private val actors = new mutable.HashSet[ActorRef]
  protected def broadcast(msg: String): Unit = actors.foreach(_ ! MessageToClient(msg))
  protected def closeConnections(): Unit = actors.foreach(_ ! PoisonPill)

  private class SocketActor(out: ActorRef) extends Actor {
    override def preStart() = actors += this.self
    override def postStop() = actors -= this.self
    def receive = {
      case msg: String =>
        config.logger.verbose(s"${this.simpleName} received message <$msg>")
        onMessage(msg)
      case MessageToClient(msg) => out ! msg
    }
  }

  protected def onMessage(msg: String) {}
  protected def onConnection() {}

  def accept = WebSocket.accept[String, String] { _ =>
    config.logger.verbose(s"${this.simpleName} received a new connection")
    onConnection()
    ActorFlow.actorRef(out => Props(new SocketActor(out)))
  }
}
