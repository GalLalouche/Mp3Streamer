package controllers

import backend.external.{ExternalLink, ExternalLinks}
import backend.mb.MbExternalLinksProvider
import common.rich.RichT._
import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Action, Controller}

object External extends Controller {
  private implicit val c = PlayConfig
  import c._
  private val external = new MbExternalLinksProvider

  private val set = Set("allmusic", "wikipedia", "lastfm", "metalarchives")
  private def toJson(e: ExternalLink[_]): (String, JsValueWrapper) = e.host.name -> e.link.address
  private def toJson(e: Traversable[ExternalLink[_]]): JsObject =
    e.filter(e => set.contains(e.host.name))
      .map(toJson).toSeq |> Json.obj
  private def toJson(e: ExternalLinks): JsObject =
    Json.obj("artist" -> toJson(e.artistLinks), "album" -> toJson(e.albumLinks))

  def get(path: String) = Action.async {
    external(Utils.parseSong(path))
      .map(toJson)
      .map(Ok(_))
  }
}
