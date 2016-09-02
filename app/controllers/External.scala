package controllers

import backend.external.{MbExternalLinksProvider, TimestampedExtendedLinks}
import backend.external.extensions.{ExtendedExternalLinks, ExtendedLink, LinkExtension}
import common.rich.RichFuture._
import common.RichJson._
import common.rich.RichT._
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Action, Controller}

import scala.concurrent.Future

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
  private def toJson(e: TimestampedExtendedLinks[_]): JsObject = toJson(e.links).mapTo(withDate(_, e.timestamp))

  def get(path: String) = Action.async {
    def toJsonOrElse(f: Future[TimestampedExtendedLinks[_]]): Future[JsObject] =
      f.map(toJson).recover {
        case e => Json.obj("error" -> e.getMessage)
      }
    val links = external(Utils.parseSong(path))
    val f = for (artistJson <- links.artistLinks |> toJsonOrElse; albumJson <- links.albumLinks |> toJsonOrElse) yield
      Json.obj("Artist" -> artistJson, "Album" -> albumJson)
    f.map(Ok(_))
  }
}
