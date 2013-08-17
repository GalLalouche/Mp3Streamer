package controllers

import java.io.File
import scala.annotation.implicitNotFound
import scala.collection.GenSeq
import scala.util.Random
import org.joda.time.format.DateTimeFormat
import akka.actor.ActorDSL
import common.Debug
import common.path.Directory
import common.LazyActor
import common.ValueTree
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
import websockets.TreeSocket
import java.net.URLDecoder
import common.path.Path._
import dirwatch.DirectoryWatcher
import websockets.NewFolderSocket

/**
  * Handles fetch requests of JSON information
  */
object Player extends Controller with Debug {
	val musicFinder = new MusicFinder {
		val dir = Directory("d:/media/music")
		val subDirs = List("Metal", "Rock", "New Age", "Classical")
		val extensions = List("mp3", "flac")
	}
	val treeFinder = MusicTree(musicFinder)
	val random = new Random
	var songs: GenSeq[File] = null

	private def songJsonInformation(song: models.Song): play.api.libs.json.JsObject = {
		song.jsonify + (("mp3", JsString("/music/songs/" + song.file.path))) +
			(("poster", JsString("/posters/" + Poster.getCoverArt(song).path)))
	}

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

	/**
	  * ************
	  * Tree
	  * ************
	  */
	implicit val system = models.KillableActors.system
	import akka.actor.ActorDSL._
	val lazyActor = ActorDSL.actor(new LazyActor(1000))
	val updatingMusic = () => timed("Updating music") {
		songs = musicFinder.getSongs.map(new File(_))
		musicTree = treeFinder.getTree
		lastUpdated = System.currentTimeMillis
		TreeSocket.actor ! TreeSocket.Update
	}

	val updater = ActorDSL.actor(new Act {
		become {
			case DirectoryWatcher.DirectoryCreated(d) =>
				NewFolderSocket.actor ! d; updatingMusic()
			case DirectoryWatcher.DirectoryDeleted(_) => updatingMusic()
		}
	})

	val watcher = ActorDSL.actor(new DirectoryWatcher(updater, musicFinder.genreDirs))

	var musicTree: ValueTree[File] = null
	var lastUpdated: Long = 0
	updatingMusic()
	private val format = DateTimeFormat.forPattern("EEE MMM dd HH:mm:ss Z yyy")
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