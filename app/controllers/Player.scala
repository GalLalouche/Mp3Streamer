package controllers

import java.io.File
import java.net.{URLDecoder, URLEncoder}

import akka.actor.{ActorDSL, actorRef2Scala}
import common.Debug
import common.concurrency.DirectoryWatcher.DirectoryEvent
import common.concurrency.{DirectoryWatcher, LazyActor}
import common.io.IODirectory
import common.rich.path.Directory
import common.rich.path.RichPath.richPath
import decoders.DbPowerampCodec
import loggers.CompositeLogger
import models._
import play.api.libs.json.{JsArray, JsString}
import play.api.mvc.{Action, Controller}
import rx.lang.scala.Subscriber
import search.Jsonable._
import search.MetadataCacher
import websockets.NewFolderSocket

import scala.util.Random

/**
 * Handles fetch requests of JSON information
 */
object Player extends Controller with Debug {
  val musicFinder = RealLocations
  private val random = new Random
  private var songPaths: Seq[File] = null

  private def songJsonInformation(song: models.Song): play.api.libs.json.JsObject = {
    SongJsonifier.jsonify(song) + (("mp3", JsString("/stream/download/" + URLEncoder.encode(song.file.path, "UTF-8")))) +
      (("poster", JsString("/posters/" + Poster.getCoverArt(song).path)))
  }

  private implicit val system = models.KillableActors.system
  private val lazyActor = ActorDSL.actor(new LazyActor(1000))

  def updateMusic() = timed("Updating music") {
    // this cannot be inlined, as it has to be the same function for LazyActor
    songPaths = musicFinder.getSongFilePaths.map(new File(_))
  }

  private def listenToDirectoryChanges(directoryEvent: DirectoryEvent) {
    directoryEvent match {
      case DirectoryWatcher.DirectoryCreated(d) =>
        MetadataCacher ! new IODirectory(d)
        lazyActor ! updateMusic
        NewFolderSocket.actor ! d
      case DirectoryWatcher.DirectoryDeleted(d) =>
        CompositeLogger warn s"Directory $d has been deleted; the index does not support deletions yet, so please update"
        lazyActor ! updateMusic
      case _ =>
        CompositeLogger debug ("DirectoryEvent:" + directoryEvent.toString)
    }
  }

  DirectoryWatcher.apply(musicFinder.genreDirs.map(_.dir)).subscribe(Subscriber(listenToDirectoryChanges))
  updateMusic()

  def randomSong = {
    val song = songPaths(random nextInt songPaths.length)
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
    val songs = Album(Directory(URLDecoder.decode(path, "UTF-8"))).songs
    songs.map(_.file).foreach(DbPowerampCodec.!)
    Ok(JsArray(songs.map(songJsonInformation)))
  }

  def song(path: String) = Action {
    Ok(songJsonInformation(Song(new File(URLDecoder.decode(path, "UTF-8")))))
  }
}
