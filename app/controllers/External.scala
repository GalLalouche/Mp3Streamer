package controllers

import backend.external.extensions.{ExtendedLink, LinkExtension, SearchExtension}
import backend.external.{ExtendedLinks, Host, MbExternalLinksProvider, TimestampedExtendedLinks}
import backend.recon.Reconcilable
import backend.recon.Reconcilable.SongExtractor
import common.RichJson._
import common.rich.RichT._
import common.rich.collections.RichSet._
import models.Song
import org.joda.time.format.ISODateTimeFormat
import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Action, Controller}
import common.rich.collections.RichTraversableOnce._

import scala.concurrent.Future
import scalaz.syntax.ToFunctorOps

object External extends Controller
    with ToFunctorOps {
  private implicit val c = PlayConfig
  private type KVPair = (String, JsValueWrapper)
  private val hosts: Seq[Host] =
    Seq(Host.MusicBrainz, Host.Wikipedia, Host.AllMusic, Host.Facebook, Host.LastFm, Host.RateYourMusic)
  private val external = new MbExternalLinksProvider

  private def toJson(e: LinkExtension[_]): KVPair = e.name -> e.link.address
  private def toJson(e: ExtendedLink[_]): KVPair = e.host.name -> Json.obj(
    "host" -> e.host.name,
    "main" -> e.link.address,
    "extensions" -> Json.obj(e.extensions.map(toJson).toSeq: _*))
  private def toJson(e: Traversable[ExtendedLink[_]]): JsObject =
    e.filterAndSortBy(_.host.canonize, hosts)
      .map(toJson) |> Json.obj
  private def toJson(e: TimestampedExtendedLinks[_]): JsObject =
    toJson(e.links).mapTo(_ + ("timestamp" -> e.timestamp.toString(ISODateTimeFormat.basicDate)))
  private def toJsonOrError(f: Future[TimestampedExtendedLinks[_]]): Future[JsObject] =
    f.map(toJson).recover {
      case e => Json.obj("error" -> e.getMessage)
    }

  private def extendMissing[R <: Reconcilable](r: R, e: TimestampedExtendedLinks[R]): TimestampedExtendedLinks[R] =
    e.copy(SearchExtension.extendMissing(hosts, r, e.links))
  def get(path: String) = Action.async {
    val song: Song = Utils parseSong path
    val links = external(song)
    val f = for (artistJson <- links.artistLinks.map(extendMissing(song.artist, _)) |> toJsonOrError;
                 albumJson <- links.albumLinks |> toJsonOrError) yield
      Json.obj("Artist links" -> artistJson, "Album links" -> albumJson)
    f.map(Ok(_))
  }
}
