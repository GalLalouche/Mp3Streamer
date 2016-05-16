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
import play.api.libs.json.{JsArray, JsString}
import play.api.mvc._
import rx.lang.scala.Subscriber
import search.Jsonable._
import search.MetadataCacher
import songs.SongSelector
import websockets.NewFolderSocket

/**
 * Handles fetch requests of JSON information
 */
object Player extends Controller with Debug {
  val musicFinder = RealLocations
  var songSelector: SongSelector = null
  def update() = timed("Updating music") {
    songSelector = SongSelector from musicFinder
    Searcher.!()
  }

  private def listenToDirectoryChanges: PartialFunction[DirectoryEvent, Unit] = {
    case DirectoryWatcher.DirectoryCreated(d) =>
      MetadataCacher ! new IODirectory(d)
      update()
      NewFolderSocket.actor ! d
    case DirectoryWatcher.DirectoryDeleted(d) =>
      common.CompositeLogger warn s"Directory $d has been deleted; the index does not support deletions yet, so please update"
      update()
    case e => common.CompositeLogger debug ("DirectoryEvent:" + e)
  }

  DirectoryWatcher.apply(musicFinder.genreDirs.map(_.dir)).subscribe(Subscriber(listenToDirectoryChanges))
  update()

  private def toJson(s: Song) =
    SongJsonifier.jsonify(s) +
        ("poster" -> JsString("/posters/" + Poster.getCoverArt(s).path)) +
        ("mp3" -> JsString("/stream/download/" + URLEncoder.encode(s.file.path, "UTF-8")))

  def randomSong = Action {
    val song: Song = songSelector.randomSong
    DbPowerampCodec ! song.file
    Ok(song |> toJson)
  }

  def album(path: String) = Action {
    val songs = Album(Directory(URLDecoder.decode(path, "UTF-8"))).songs
    songs.map(_.file).foreach(DbPowerampCodec.!)
    Ok(JsArray(songs map toJson))
  }

  private def toSong(path: String): Song = Song(new File(URLDecoder.decode(path, "UTF-8")))
  def song(path: String) = Action {
    Ok(path |> toSong |> toJson)
  }

  def nextSong(path: String) = Action {
    Ok(path |> toSong |> songSelector.followingSong |> toJson)
  }
}
