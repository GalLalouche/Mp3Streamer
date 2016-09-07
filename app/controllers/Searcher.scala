package controllers

import java.net.URLDecoder

import common.concurrency.Extra
import common.rich.RichT.richT
import play.api.Logger
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.libs.json.{JsArray, Json}
import play.api.mvc.{Action, Controller}
import search.{CompositeIndex, Jsonable}

object Searcher extends Controller with Extra {
  private implicit val c = PlayConfig
  import c._
  private var index: CompositeIndex = new CompositeIndex
  override def apply() {
    index = new CompositeIndex
    Logger info "Search engine has been updated"
  }
  def search(path: String) = Action {
    def toJsArray[T: Jsonable](results: Seq[T]): JsArray =
      results.map(implicitly[Jsonable[T]].jsonify) |> JsArray
    val terms = URLDecoder.decode(path, "UTF-8") split " "
    val (songs, albums, artists) = index search terms

    Ok(Json obj(("songs", toJsArray(songs)), ("albums", toJsArray(albums)), ("artists", toJsArray(artists))))
  }
}
