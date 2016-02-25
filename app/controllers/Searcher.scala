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

object Searcher extends Controller {
  private val indexBuilder = TermIndexBuilder
  private def buildIndexFromCache[T: Jsonable: Indexable](implicit m: Manifest[T]) =
    indexBuilder.buildIndexFor(MetadataCacher.load[T])
  var songIndex: Index[Song] = null
  var albumIndex: Index[Album] = null
  var artistIndex: Index[Artist] = null
  update
  def update() {
    songIndex = buildIndexFromCache
    albumIndex = buildIndexFromCache
    artistIndex = buildIndexFromCache
  }

  def search(path: String) = Action {
    val query = URLDecoder.decode(path, "UTF-8")
    val terms = query split " "
    def toArray[T: Jsonable](index: Index[T]): JsArray =
      index.findIntersection(terms).map(implicitly[Jsonable[T]].jsonify).mapTo(JsArray)
    val songs = toArray(songIndex)
    val albums = toArray(albumIndex)
    val artists = toArray(artistIndex)
    Ok(Json obj ("songs" -> songs, "albums" -> albums, "artists" -> artists))
  }
}