package controllers

import backend.external.{ExternalLink, ExternalLinks, ExternalLinksProvider}
import backend.mb.MbExternalLinksProvider
import common.rich.RichT._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Action, Controller}

object External extends Controller {
  private val external: ExternalLinksProvider = new MbExternalLinksProvider()

  private val set = Set("allmusic", "wikipedia", "lastfm")
  private def toJson(e: ExternalLink): (String, JsValueWrapper) =
    e.host.name -> e.link.address
  private def toJson(e: Traversable[ExternalLink]): JsObject =
    e.filter(e => set.contains(e.host.name))
      .map(toJson).toSeq |> Json.obj
  private def toJson(e: ExternalLinks): JsObject =
    Json.obj("artist" -> toJson(e.artistLinks), "album" -> toJson(e.albumLinks))

  def get(path: String) = Action.async {
    external.getExternalLinks(Utils.parseSong(path))
      .map(toJson)
      .map(Ok(_))
  }
}
