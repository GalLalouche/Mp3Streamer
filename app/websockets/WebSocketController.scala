package websockets

import play.api.mvc._
import akka.actor.Actor
import scala.concurrent.Future
import play.api.libs.iteratee._
import akka.actor.PoisonPill
import akka.actor.ActorDSL
import common.LazyActor

private[websockets] trait WebSocketController extends Controller {
	val out = Concurrent.broadcast[String];

	implicit val system = models.KillableActors.system
	val name = getClass.getSimpleName
	def receive: PartialFunction[Any, Unit] = {
		case _ => ()
	}
	lazy val actor = ActorDSL.actor(new WebSocketController.DisconnectingActor(receive, out._2, name))
	def accept = WebSocket.async[String] { r =>
		{
			val i = Iteratee.foreach[String] {
				x => loggers.CompositeLogger.debug("New subscriber to " + getClass())
			}
			Future.successful(i, out._1)
		}
	}
}

object WebSocketController {
	class DisconnectingActor(_receive: PartialFunction[Any, Unit], channel: play.api.libs.iteratee.Concurrent.Channel[_], name: String) extends Actor {
		override def receive = _receive
		override def postStop {
			loggers.CompositeLogger.trace("Ending " + name + " socket connection")
			channel.eofAndEnd
		}
	}
}