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
import models.MusicSearcher
import models.SimpleMusicSearcher
import play.api.libs.json.JsObject
import play.api.libs.json.Json
import common.rich.path.RichFile._

object Searcher extends Controller {
	lazy val searcher = new SimpleMusicSearcher(new File("D:/Media/Music/songs.json").lines.map(Json.parse).map(_.as[JsObject]).map(Song.apply))
	def search(path: String) = Action {
		val findSongs: Seq[Song] = searcher(path)
		Ok(JsArray(findSongs.map(_.jsonify)))
	}
}