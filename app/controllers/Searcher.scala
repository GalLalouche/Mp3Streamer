package controllers

import java.net.URLDecoder
import common.rich.RichT.richT
import models.{ Album, Artist, Song }
import play.api.libs.json.{ JsArray, Json }
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.mvc.{ Action, Controller }
import search.{ Index, Indexable }
import search.{ MetadataCacher, TermIndexBuilder }
import search.Indexable._
import search.Jsonable
import search.Jsonable._
import search.CompositeIndex

object Searcher extends Controller {
  private val index = new CompositeIndex(TermIndexBuilder)
  private def toArray[T: Jsonable](results: Seq[T]): JsArray =
    results.map(implicitly[Jsonable[T]].jsonify).mapTo(JsArray)
  def search(path: String) = Action {
    val query = URLDecoder.decode(path, "UTF-8")
    val terms = query split " "
    val (songs, albums, artists) = index.search(terms)

    Ok(Json obj ("songs" -> toArray(songs), "albums" -> toArray(albums), "artists" -> toArray(artists)))
  }
}