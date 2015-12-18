package websockets

import common.Debug
import common.rich.path.Directory

/** Notifies the client when a new folder has been added */
object NewFolderSocket extends WebSocketController with Debug {
	override def receive = {
		case x: Directory if (x.files.isEmpty == false) =>
			println("Found new folder " + x)
			out._2.push(x.path)
	}
}