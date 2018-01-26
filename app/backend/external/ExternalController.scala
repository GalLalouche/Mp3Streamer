package backend.external

import java.time.LocalDateTime

import backend.external.extensions.{ExtendedLink, LinkExtension, SearchExtension}
import backend.recon.Reconcilable.SongExtractor
import backend.recon._
import common.RichJson._
import common.rich.RichT._
import common.rich.collections.RichTraversableOnce._
import controllers.{ControllerUtils, LegacyController}
import models.Song
import play.api.libs.json.{JsObject, JsString, Json}
import play.api.mvc.{Action, Result}

import scala.concurrent.Future
import scalaz.std.FutureInstances
import scalaz.syntax.ToBindOps

object ExternalController extends LegacyController
    with FutureInstances with ToBindOps {
  import ControllerUtils.config
  private type KVPair = (String, play.api.libs.json.Json.JsValueWrapper)
  private val hosts: Seq[Host] =
    Seq(Host.MusicBrainz, Host.Wikipedia, Host.AllMusic, Host.Facebook, Host.LastFm, Host.RateYourMusic)
  private val external = new MbExternalLinksProvider

  private def toJson(e: LinkExtension[_]): KVPair = e.name -> e.link.address
  private def toJson(e: ExtendedLink[_]): KVPair = e.host.name -> Json.obj(
    "host" -> e.host.name.mapIf(e.isNew.const).to(_ + "*"),
    "main" -> e.link.address,
    "extensions" -> Json.obj(e.extensions.map(toJson).toSeq: _*))
  private def toJson(e: Traversable[ExtendedLink[_]]): JsObject =
    e.filterAndSortBy(_.host.canonize, hosts).map(toJson) |> Json.obj

  private def toDateString(l: LocalDateTime): String = f"${l.getDayOfMonth}%02d/${l.getMonthValue}%02d"

  private def toJson(e: TimestampedExtendedLinks[_]): JsObject =
    toJson(e.links).mapTo(_ + ("timestamp" -> JsString(e.timestamp |> toDateString)))
  private def toJsonOrError(f: Future[TimestampedExtendedLinks[_]]): Future[JsObject] =
    f.map(toJson).recover {
      case e => Json.obj("error" -> e.getMessage)
    }

  def get(path: String) = Action.async {
    val song: Song = ControllerUtils parseSong path
    getLinks(song)
  }

  def getLinks(song: Song): Future[Result] = {
    val links = external(song)
    val extendMissing = TimestampedExtendedLinks.links.modify(SearchExtension.extendMissing(hosts, song.artist))
    val f = for {
      artistJson <- links.artistLinks.map(extendMissing) |> toJsonOrError
      albumJson <- links.albumLinks |> toJsonOrError
    } yield Json.obj("Artist links" -> artistJson, "Album links" -> albumJson)
    f.map(Ok(_))
  }
  def refresh(path: String) = Action.async {
    val song: Song = ControllerUtils parseSong path
    external.delete(song) >> getLinks(song)
  }
  def updateRecon(path: String) = Action.async {request =>
    val json = request.body.asJson.get
    def getReconId(s: String) = json ostr s map ReconID
    val song: Song = ControllerUtils parseSong path
    external.updateRecon(song, artistReconId = getReconId("artist"), albumReconId = getReconId("album"))
        .>>(getLinks(song))
  }
}
