package controllers

import java.io.File
import java.net.{ URLDecoder, URLEncoder }
import scala.util.Random
import akka.actor.{ ActorDSL, actorRef2Scala }
import common.{ DaemonRunner, Debug, LazyActor }
import common.rich.path.Directory
import common.rich.path.RichPath.richPath
import decoders.DbPowerampCodec
import dirwatch.DirectoryWatcher
import models._
import play.api.libs.json.{ JsArray, JsString }
import play.api.mvc.{ Action, Controller }
import websockets.{ NewFolderSocket, TreeSocket }

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
    songs = getSongFilePaths.map(new File(_))
    TreeSocket.actor ! TreeSocket.Update
  }

  private val watcher = ActorDSL.actor(new DirectoryWatcher(ActorDSL.actor(new Act {
    become {
      case DirectoryWatcher.DirectoryCreated(d) =>
        lazyActor ! updatingMusic
        NewFolderSocket.actor ! d
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
    def decode(f: File) { Streamer.decoder.encodeFileIfNeeded(f) } // returns a unit as required by DaemonRunner
    val songs = AlbumDirectory(new File(URLDecoder.decode(path, "UTF-8"))).songs
    songs.map(_.file).map(e => () => decode(e)).foreach(DaemonRunner.run)
    Ok(JsArray(songs.map(songJsonInformation)))
  }
}
