package controllers

import backend.external.MbExternalLinksProvider
import backend.external.extensions.{ExtendedExternalLinks, ExtendedLink, LinkExtension}
import common.RichJson._
import common.rich.RichT._
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Action, Controller}

object External extends Controller {
  private implicit val c = PlayConfig
  private val external = new MbExternalLinksProvider

  private val set = Set("allmusic", "wikipedia", "lastfm", "metalarchives", "musicbrainz", "facebook")
      .flatMap(e => List(e, e + "*"))
  private def withDate(js: JsObject, d: DateTime): JsObject = js + ("timestamp" -> d.toString(ISODateTimeFormat.basicDate))
  private def toJson(e: LinkExtension[_]): (String, JsValueWrapper) = e.name -> e.link.address
  private def toJson(e: ExtendedLink[_]): (String, JsValueWrapper) = e.host.name -> Json.obj(
    "host" -> e.host.name,
    "main" -> e.link.address,
    "extensions" -> Json.obj(e.extensions.toSeq.map(toJson): _*))
  private def toJson(e: Traversable[ExtendedLink[_]]): JsObject = e
      .filter(e => e.host.canonize.name.toLowerCase |> set)
      .map(toJson).toSeq |> Json.obj
  private def toJson(e: ExtendedExternalLinks): JsObject = Json.obj(
    "artist" -> toJson(e.artistLinks.links).mapTo(withDate(_, e.artistLinks.timestamp)),
    "album" -> toJson(e.albumLinks.links).mapTo(withDate(_, e.albumLinks.timestamp)))

  def get(path: String) = Action.async {
    external(Utils.parseSong(path))
        .map(toJson)
        .map(Ok(_))
  }
}
