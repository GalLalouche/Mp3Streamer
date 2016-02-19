package controllers

import java.io.File
import java.net.{ URLDecoder, URLEncoder }
import scala.util.Random
import akka.actor.ActorDSL
import akka.actor.ActorDSL.Act
import akka.actor.actorRef2Scala
import common.{ Debug, LazyActor }
import common.rich.path.RichPath.richPath
import dirwatch.DirectoryWatcher
import models.{ AlbumDirectory, MusicFinder, Poster, Song }
import play.api.libs.json.{ JsArray, JsString }
import play.api.mvc.{ Action, Controller }
import websockets.{ NewFolderSocket, TreeSocket }
import search.Index
import search.SimpleIndexBuilder
import play.api.libs.json.JsObject
import play.api.libs.json.Json
import common.rich.path.RichFile._
import search.TermIndexBuilder

object Searcher extends Controller {
  lazy val songs = new File("D:/Media/Music/songs.json")
      .lines
      .map(Json.parse)
      .map(_.as[JsObject])
      .map(Song.apply)
  lazy val indexBuilder = TermIndexBuilder 
  lazy val index = TermIndexBuilder.buildIndexFor(songs)
  def search(path: String) = Action {
    Ok(JsArray(index.find(URLDecoder.decode(path, "UTF-8")).map(_.jsonify)))
  }
}