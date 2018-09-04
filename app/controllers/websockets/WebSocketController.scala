package controllers.websockets

import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

import akka.actor.{Actor, ActorRef, ActorSystem, PoisonPill, Props}
import akka.stream.ActorMaterializer
import backend.logging.Logger
import common.rich.RichT._
import play.api.libs.streams.ActorFlow
import play.api.mvc.{InjectedController, WebSocket}

import scala.collection.JavaConverters._

// This has to appear before the trait, otherwise materializer won't be available as an implicit val?!
object WebSocketController {
  private case class MessageToClient(str: String) extends AnyVal
  private implicit val system: ActorSystem = ActorSystem("WebSockets")
  private implicit val materializer: ActorMaterializer = ActorMaterializer()(system)

  // A poor man's singleton class
  private val actors = new ConcurrentHashMap[Class[_], java.util.Set[ActorRef]]
}

// TODO replace with composition
class WebSocketController(logger: Logger) extends InjectedController {
  import WebSocketController._

  private lazy val myActors = {
    // TODO Semigroup mutable map
    actors.putIfAbsent( // TODO extract this newSet BS to a helper class.
      getClass, Collections.newSetFromMap[ActorRef](new ConcurrentHashMap[ActorRef, java.lang.Boolean]()))
    actors.get(getClass).asScala
  }

  protected def broadcast(msg: String): Unit = myActors.foreach(_ ! MessageToClient(msg))
  protected def closeConnections(): Unit = myActors.foreach(_ ! PoisonPill)

  private class SocketActor(out: ActorRef) extends Actor {
    override def preStart() = myActors += this.self
    override def postStop() = myActors -= this.self
    def receive = {
      case msg: String =>
        logger.verbose(s"${this.simpleName} received message <$msg>")
        onMessage(msg)
      case MessageToClient(msg) => out ! msg
    }
  }

  protected def onMessage(msg: String) {}
  protected def onConnection() {}

  def accept = WebSocket.accept[String, String] {_ =>
    //config.logger.verbose(s"${this.simpleName} received a new connection")
    onConnection()
    ActorFlow.actorRef(out => Props(new SocketActor(out)))
  }
}
