package controllers

import java.io.File
import java.net.{URLDecoder, URLEncoder}

import common.Debug
import common.concurrency.DirectoryWatcher
import common.concurrency.DirectoryWatcher.DirectoryEvent
import common.io.IODirectory
import common.rich.RichT._
import common.rich.path.Directory
import common.rich.path.RichFile._
import decoders.DbPowerampCodec
import models._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.{JsArray, JsString}
import play.api.mvc._
import rx.lang.scala.Subscriber
import search.Jsonable._
import search.MetadataCacher
import songs.SongSelector
import websockets.NewFolderSocket

/** Handles fetch requests of JSON information, and listens to directory changes. */
object Player extends Controller with Debug {
  private val musicFinder = RealLocations
  private var songSelector: SongSelector = null
  def update() = timed("Updating music") {
    songSelector = SongSelector from musicFinder
  }
  //TODO hide this, shouldn't be a part of the controller
  private def directoryListener(e: DirectoryEvent) = e match {
    case DirectoryWatcher.DirectoryCreated(d) =>
      MetadataCacher ! new IODirectory(d) onComplete (e => {
        Searcher.!()
      })
      update()
      NewFolderSocket.actor ! d
    case DirectoryWatcher.DirectoryDeleted(d) =>
      common.CompositeLogger warn s"Directory $d has been deleted; the index does not support deletions yet, so please update"
      update()
    case e => common.CompositeLogger debug ("DirectoryEvent:" + e)
  }
  DirectoryWatcher.apply(musicFinder.genreDirs.map(_.dir)).subscribe(Subscriber(directoryListener))
  update()

  private def toJson(s: Song) = {
    DbPowerampCodec ! s.file
    SongJsonifier.jsonify(s) +
        ("poster" -> JsString("/posters/" + Poster.getCoverArt(s).path)) +
        ("mp3" -> JsString("/stream/download/" + URLEncoder.encode(s.file.path, "UTF-8")))
  }

  def randomSong = Action {
    Ok(songSelector.randomSong |> toJson)
  }

  def album(path: String) = Action {
    Ok(Directory(URLDecoder.decode(path, "UTF-8")) |> Album.apply |> (_.songs.map(toJson)) |> JsArray.apply)
  }

  private def toSong(path: String): Song = Song(new File(URLDecoder.decode(path, "UTF-8")))

  def song(path: String) = Action {
    Ok(path |> toSong |> toJson)
  }

  def nextSong(path: String) = Action {
    Ok(path |> toSong |> songSelector.followingSong |> toJson)
  }
}
