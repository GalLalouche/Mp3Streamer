package controllers.websockets

import akka.actor.{Actor, ActorDSL}
import backend.logging.Logger
import common.rich.RichT._
import controllers.Utils
import play.api.libs.iteratee.{Concurrent, Iteratee}
import play.api.mvc.{Controller, WebSocket}

object WebSocketController {
  private val logger: Logger = Utils.config.logger
  private class DisconnectingActor(_receive: PartialFunction[Any, Unit], channel: play.api.libs.iteratee.Concurrent.Channel[_], name: String) extends Actor {
    override def receive = _receive
    override def postStop {
      logger verbose s"Ending $name socket connection"
      channel.eofAndEnd
    }
  }
}

trait WebSocketController extends Controller {
  import Utils.config._
  private val out = Concurrent.broadcast[String]
  def safePush(msg: String) {
    Option(out).flatMap(e => Option(e._2)).foreach(_ push msg)
  }

  implicit val system = models.KillableActors.system
  val name = getClass.getSimpleName
  def receive: PartialFunction[Any, Unit] = {
    case _ => ()
  }
  lazy val actor = ActorDSL.actor(new WebSocketController.DisconnectingActor(receive, out._2, name))
  protected def onMessage(msg: String) {}
  protected def onConnection() {}
  def accept = WebSocket.using[String] { r => {
    logger.verbose(s"${this.simpleName} received a new connection")
    onConnection()
    val i = Iteratee.foreach[String] { x =>
      logger.verbose(s"${this.simpleName} received message <$x>")
      onMessage(x)
    }
    (i, out._1)
  }
  }
}
