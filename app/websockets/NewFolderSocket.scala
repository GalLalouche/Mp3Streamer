package websockets

import scala.actors.Actor
import scala.actors.Exit
import scala.concurrent.Future
import play.api.libs.iteratee.Concurrent
import play.api.libs.iteratee.Iteratee
import play.api.mvc.Controller
import play.api.mvc.WebSocket
import common.path.Directory
import common.path.Path._
import common.Debug

/**
  * Sends console information to the listeners
  */
object NewFolderSocket extends WebSocketController with Debug {
	override def receive = {
		case x: Directory if (x.files.isEmpty == false) => out._2.push(x.path)
	}
}