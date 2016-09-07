package controllers

import backend.external.extensions.{ExtendedLink, LinkExtension}
import backend.external.{MbExternalLinksProvider, TimestampedExtendedLinks}
import common.RichJson._
import common.rich.RichT._
import org.joda.time.format.ISODateTimeFormat
import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Action, Controller}

import scala.concurrent.Future

object External extends Controller {
  private implicit val c = PlayConfig
  private type KVPair = (String, JsValueWrapper)
  private val allowedHosts = Set("allmusic", "wikipedia", "lastfm", "metalarchives", "musicbrainz", "facebook")
      .flatMap(e => List(e, e + "*"))
  private val external = new MbExternalLinksProvider

  private def toJson(e: LinkExtension[_]): KVPair = e.name -> e.link.address
  private def toJson(e: ExtendedLink[_]): KVPair = e.host.name -> Json.obj(
    "host" -> e.host.name,
    "main" -> e.link.address,
    "extensions" -> Json.obj(e.extensions.map(toJson).toSeq: _*))
  private def toJson(e: Traversable[ExtendedLink[_]]): JsObject = e
      .filter(_.host.canonize.name.toLowerCase |> allowedHosts)
      .map(toJson).toSeq |> Json.obj
  private def toJson(e: TimestampedExtendedLinks[_]): JsObject =
    toJson(e.links).mapTo(_ + ("timestamp" -> e.timestamp.toString(ISODateTimeFormat.basicDate)))
  private def toJsonOrError(f: Future[TimestampedExtendedLinks[_]]): Future[JsObject] =
    f.map(toJson).recover {
      case e => Json.obj("error" -> e.getMessage)
    }

  def get(path: String) = Action.async {
    val links = external(Utils parseSong path)
    val f = for (artistJson <- links.artistLinks |> toJsonOrError; albumJson <- links.albumLinks |> toJsonOrError) yield
      Json.obj("Artist links" -> artistJson, "Album links" -> albumJson)
    f.map(Ok(_))
  }
}
