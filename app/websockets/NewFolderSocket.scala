package websockets

import common.Debug
import common.rich.path.Directory

/**
  * Sends console information to the listeners
  */
object NewFolderSocket extends WebSocketController with Debug {
	override def receive = {
		case x: Directory if (x.files.isEmpty == false) => out._2.push(x.path)
	}
}