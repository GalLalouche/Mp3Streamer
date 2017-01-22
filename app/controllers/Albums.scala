package controllers

import backend.recon.{Album, Artist}
import common.Debug
import common.rich.RichT._
import mains.albums.NewAlbums
import play.api.libs.json._
import play.api.mvc._

/** Handles fetch requests of JSON information, and listens to directory changes. */
object Albums extends Controller with Debug {
  import Utils.config
  private val newAlbums = new NewAlbums()
  private def toJson(a: Album): JsObject =
    Json.obj("title" -> a.title, "year" -> a.year)
  private def toJson(a: Artist, albums: Seq[Album]): JsObject =
    Json.obj("artistName" -> a.name, "albums" -> JsArray(albums map toJson))
  private def toJson(m: Map[Artist, Seq[Album]]): JsArray =
    m.map(e => toJson(e._1, e._2)).toSeq |> JsArray.apply
  def albums = Action.async {
    newAlbums.load.map(toJson).map(Ok(_))
  }

  def index = Action {
    Ok(views.html.new_albums())
  }
}
