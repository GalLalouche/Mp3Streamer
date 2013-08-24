package websockets

import models.MusicTree
import controllers.MusicLocations
import play.api.mvc.Action
import play.api.http.HeaderNames
import common.ValueTree
import java.io.File
import org.joda.time.format.DateTimeFormat

/**
  * updates changs about the music tree
  */
object TreeSocket extends WebSocketController with MusicLocations with MusicTree {
	case object Update
	private val format = DateTimeFormat.forPattern("EEE MMM dd HH:mm:ss Z yyy")

	private var lastUpdated: Long = 0
	private var musicTree: ValueTree[File] = null
	override def receive = {
		case Update => updateTree
	}

	private def updateTree {
		lastUpdated = System.currentTimeMillis
		musicTree = getTree
		loggers.CompositeLogger.trace("sending tree update to socket client")
		out._2.push("tree")
	}

	def tree = Action { request =>
		{
			val dateString = request.headers.get(HeaderNames.IF_MODIFIED_SINCE).getOrElse(format.print(0).toString)
			val lastModified = format.parseDateTime(dateString).getMillis
			if (lastUpdated - 1000 < lastModified) // -1000 for one second margin of error
				NotModified
			else
				Ok(MusicTree.jsonify(musicTree)).withHeaders(
					HeaderNames.LAST_MODIFIED -> format.print(lastUpdated + 1000))
		}
	}
}