package websockets

import scala.actors.Actor
import scala.actors.Exit
import scala.concurrent.Future

import play.api.libs.iteratee.Concurrent
import play.api.libs.iteratee.Iteratee
import play.api.mvc.Controller
import play.api.mvc.WebSocket

/**
  * updates changs about the music tree
  */
object TreeSocket extends Controller with Actor {
	start()
	def act() {
		while (true) {
			receive {
				case Exit => disconnect
				case "Update" => updateTree
			}
		}
	}

	val out = Concurrent.broadcast[String]

	def disconnect = {
		loggers.CompositeLogger.trace("Ending tree socket connection")
		out._2.eofAndEnd
	}
	

	def updateTree {
		loggers.CompositeLogger.trace("sending tree update to socket client")
		out._2.push("tree")
	}

	def tree = WebSocket.async[String] {
		request =>
			{
				val i = Iteratee.foreach[String] {
					x => loggers.CompositeLogger.debug("New subscriber to TreeSocket")
				}
				Future.successful(i, out._1)
			}
	}
}