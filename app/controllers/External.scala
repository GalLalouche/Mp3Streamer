package controllers

import backend.external.{ExtendedExternalLinks, ExtendedLink, LinkExtensions}
import backend.mb.MbExternalLinksProvider
import common.rich.RichT._
import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.json.{JsArray, JsObject, Json}
import play.api.mvc.{Action, Controller}

object External extends Controller {
  private implicit val c = PlayConfig
  private val external = new MbExternalLinksProvider

  private val set = Set("allmusic", "wikipedia", "lastfm", "metalarchives", "musicbrainz", "facebook")
      .flatMap(e => List(e, e + "*"))
  private def toJson(e: LinkExtensions[_]): JsObject = Json obj (e.name -> e.link.address)
  private def toJson(e: ExtendedLink[_]): (String, JsValueWrapper) = e.host.name -> Json.obj(
    "host" -> e.host.name,
    "main" -> e.link.address,
    "extensions" -> JsArray(e.extensions.toSeq map toJson))
  private def toJson(e: Traversable[ExtendedLink[_]]): JsObject = e
      .filter(e => set.contains(e.host.name))
      .map(toJson).toSeq |> Json.obj
  private def toJson(e: ExtendedExternalLinks): JsObject =
    Json.obj("artist" -> toJson(e.artistLinks), "album" -> toJson(e.albumLinks))

  def get(path: String) = Action.async {
    external(Utils.parseSong(path))
        .map(toJson)
        .map(Ok(_))
  }
}
