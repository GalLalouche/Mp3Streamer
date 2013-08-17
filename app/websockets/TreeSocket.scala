package websockets

/**
  * updates changs about the music tree
  */
object TreeSocket extends WebSocketController {
	case object Update
	override def receive = {
		case Update => updateTree
	} 
	private def updateTree {
		loggers.CompositeLogger.trace("sending tree update to socket client")
		out._2.push("tree")
	}
}