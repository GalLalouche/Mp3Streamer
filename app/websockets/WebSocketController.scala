package websockets

import akka.actor.{Actor, ActorDSL}
import play.api.libs.iteratee.{Concurrent, Iteratee}
import play.api.mvc.{Controller, WebSocket}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait WebSocketController extends Controller {
  private val out = Concurrent.broadcast[String]
  def safePush(msg: String) { Option(out).flatMap(e => Option(e._2)).foreach(_.push(msg)) }

  implicit val system = models.KillableActors.system
  val name = getClass.getSimpleName
  def receive: PartialFunction[Any, Unit] = {
    case _ => ()
  }
  lazy val actor = ActorDSL.actor(new WebSocketController.DisconnectingActor(receive, out._2, name))
  def accept = WebSocket.using[String] { r => {
    val i = Iteratee.foreach[String] {
      x => common.CompositeLogger.debug("New subscriber to " + getClass())
    }
    (i, out._1)
  }}
}

object WebSocketController {
  private class DisconnectingActor(_receive: PartialFunction[Any, Unit], channel: play.api.libs.iteratee.Concurrent.Channel[_], name: String) extends Actor {
    override def receive = _receive
    override def postStop {
      common.CompositeLogger.trace("Ending " + name + " socket connection")
      channel.eofAndEnd
    }
  }
}
