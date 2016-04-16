package controllers

import java.io.File
import java.net.{URLDecoder, URLEncoder}

import common.Debug
import common.concurrency.DirectoryWatcher
import common.concurrency.DirectoryWatcher.DirectoryEvent
import common.io.IODirectory
import common.rich.path.Directory
import common.rich.path.RichFile._
import decoders.DbPowerampCodec
import models._
import play.api.libs.json.{JsArray, JsString}
import play.api.mvc._
import rx.lang.scala.Subscriber
import search.Jsonable._
import search.MetadataCacher
import websockets.NewFolderSocket

import scala.util.Random

/**
 * Handles fetch requests of JSON information
 */
class Player extends Controller with Debug {
  val musicFinder = RealLocations
  private val random = new Random
  private var songPaths: Seq[File] = null

  private def songJsonInformation(song: models.Song): play.api.libs.json.JsObject = {
    SongJsonifier.jsonify(song) + (("mp3", JsString("/stream/download/" + URLEncoder.encode(song.file.path, "UTF-8")))) +
      (("poster", JsString("/posters/" + Poster.getCoverArt(song).path)))
  }

  private implicit val system = models.KillableActors.system

  def update() = timed("Updating music") {
    songPaths = musicFinder.getSongFilePaths.map(new File(_))
    Action {
      // this cannot be inlined, as it has to be the same function for LazyActor
      Ok("Updated")
    }
  }

  private def listenToDirectoryChanges(directoryEvent: DirectoryEvent) {
    directoryEvent match {
      case DirectoryWatcher.DirectoryCreated(d) =>
        MetadataCacher ! new IODirectory(d)
        update
//        NewFolderSocket.actor ! d
      case DirectoryWatcher.DirectoryDeleted(d) =>
        common.CompositeLogger warn s"Directory $d has been deleted; the index does not support deletions yet, so please update"
        update
      case _ =>
        common.CompositeLogger debug ("DirectoryEvent:" + directoryEvent.toString)
    }
  }

  DirectoryWatcher.apply(musicFinder.genreDirs.map(_.dir)).subscribe(Subscriber(listenToDirectoryChanges))
  update()

  def randomSong = {
    val song = songPaths(random nextInt songPaths.length)
    Action {
      try {
        Ok(songJsonInformation(Song(song)))
      } catch {
        case e: Exception =>
          common.CompositeLogger.error("Failed to parse " + song.path, e)
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
