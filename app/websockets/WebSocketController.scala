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
	val lazyActor = ActorDSL.actor(new WebSocketController.DisconnectingActor(out._2, name))
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
	class DisconnectingActor(channel: play.api.libs.iteratee.Concurrent.Channel[String], name: String) extends Actor {
		override def receive = {
			case _ => ()
		}
		override def postStop {
			loggers.CompositeLogger.trace("Ending " + name + " socket connection")
			channel.eofAndEnd
		}
	}
}