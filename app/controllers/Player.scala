package controllers

import java.io.File
import java.net.URLDecoder
import scala.collection.GenSeq
import scala.util.Random
import org.joda.time.format.DateTimeFormat
import akka.actor.ActorDSL
import akka.actor.ActorDSL.Act
import akka.actor.actorRef2Scala
import common.Debug
import common.LazyActor
import common.ValueTree
import common.path.Directory
import common.path.Path.richPath
import dirwatch.DirectoryWatcher
import models.Album
import models.MusicFinder
import models.MusicTree
import models.Poster
import models.Song
import play.api.http.HeaderNames
import play.api.libs.json.JsArray
import play.api.libs.json.JsString
import play.api.mvc.Action
import play.api.mvc.Controller
import websockets.NewFolderSocket
import websockets.TreeSocket
import java.net.URLEncoder

/**
  * Handles fetch requests of JSON information
  */
object Player extends Controller with MusicFinder with MusicLocations with Debug {
	private val random = new Random
	private var songs: Seq[File] = null

	private def songJsonInformation(song: models.Song): play.api.libs.json.JsObject = {
		song.jsonify + (("mp3", JsString("/music/songs/" + URLEncoder.encode(song.file.path, "UTF-8")))) +
			(("poster", JsString("/posters/" + Poster.getCoverArt(song).path)))
	}

	private implicit val system = models.KillableActors.system
	import akka.actor.ActorDSL._
	private val lazyActor = ActorDSL.actor(new LazyActor(1000))

	private val updatingMusic = () => timed("Updating music") {
		// this cannot be inlined, as it has to be the same function for LazyActor
		songs = getSongs.map(new File(_))
		TreeSocket.actor ! TreeSocket.Update
	}

	private val watcher = ActorDSL.actor(new DirectoryWatcher(ActorDSL.actor(new Act {
		become {
			case DirectoryWatcher.DirectoryCreated(d) =>
				NewFolderSocket.actor ! d; lazyActor ! updatingMusic
			case DirectoryWatcher.DirectoryDeleted(_) => lazyActor ! updatingMusic
		}
	}), genreDirs))
	updatingMusic()

	def randomSong = {
		val song = songs(random nextInt (songs length))
		Action {
			try {
				Ok(songJsonInformation(Song(song)))
			} catch {
				case e: Exception =>
					loggers.CompositeLogger.error("Failed to parse " + song.path, e)
					InternalServerError
			}
		}
	}

	def album(path: String) = Action {
		Ok(JsArray(Album(new File(URLDecoder.decode(path, "UTF-8"))).songs.map(songJsonInformation)))
	}
}