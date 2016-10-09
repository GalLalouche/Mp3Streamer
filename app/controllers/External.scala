package controllers

import backend.external.extensions.{ExtendedLink, LinkExtension, SearchExtension}
import backend.external.{ExtendedLinks, Host, MbExternalLinksProvider, TimestampedExtendedLinks}
import backend.recon.Reconcilable
import backend.recon.Reconcilable.SongExtractor
import common.RichJson._
import common.rich.RichT._
import common.rich.collections.RichSet._
import common.rich.func.MoreMonadPlus._
import models.Song
import org.joda.time.format.ISODateTimeFormat
import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Action, Controller}

import scala.concurrent.Future
import scalaz.syntax.ToFunctorOps

object External extends Controller
    with ToFunctorOps {
  private implicit val c = PlayConfig
  private type KVPair = (String, JsValueWrapper)
  private val hosts: Seq[Host] =
    Seq(Host.MusicBrainz, Host.Wikipedia, Host.AllMusic, Host.Facebook, Host.LastFm, Host.RateYourMusic)
  //TODO move to somewhere more common
  private def filterAndSortBy[T, S](actual: Traversable[T])(f: T => S, order: Seq[S]): Seq[T] = {
    val orderMap = order.zipWithIndex.toMap
    actual.toSeq
        .fproduct(f)
        .filter(_._2 |> orderMap.contains)
        .sortBy(_._2 |> orderMap)
        .map(_._1)
  }
  private val external = new MbExternalLinksProvider

  private def toJson(e: LinkExtension[_]): KVPair = e.name -> e.link.address
  private def toJson(e: ExtendedLink[_]): KVPair = e.host.name -> Json.obj(
    "host" -> e.host.name,
    "main" -> e.link.address,
    "extensions" -> Json.obj(e.extensions.map(toJson).toSeq: _*))
  private def toJson(e: Traversable[ExtendedLink[_]]): JsObject =
    filterAndSortBy(e)(_.host.canonize, hosts)
      .map(toJson) |> Json.obj
  private def toJson(e: TimestampedExtendedLinks[_]): JsObject =
    toJson(e.links).mapTo(_ + ("timestamp" -> e.timestamp.toString(ISODateTimeFormat.basicDate)))
  private def toJsonOrError(f: Future[TimestampedExtendedLinks[_]]): Future[JsObject] =
    f.map(toJson).recover {
      case e => Json.obj("error" -> e.getMessage)
    }

  private def extendMissing[R <: Reconcilable](r: R, links: ExtendedLinks[R]): ExtendedLinks[R] =
    links ++ (hosts.toSet \ links.map(_.host.canonize).toSet map (SearchExtension(_, r)))
  private def extendMissing[R <: Reconcilable](r: R, e: TimestampedExtendedLinks[R]): TimestampedExtendedLinks[R] =
    e.copy(extendMissing(r, e.links))
  def get(path: String) = Action.async {
    val song: Song = Utils parseSong path
    val links = external(song)
    val f = for (artistJson <- links.artistLinks.map(extendMissing(song.artist, _)) |> toJsonOrError;
                 albumJson <- links.albumLinks |> toJsonOrError) yield
      Json.obj("Artist links" -> artistJson, "Album links" -> albumJson)
    f.map(Ok(_))
  }

  def main(args: Array[String]): Unit = {
    filterAndSortBy(Set("d" ,"two", "three", "ag", "b"))(_.length, Seq(2, 8, 3, 1)).log()
  }
}
