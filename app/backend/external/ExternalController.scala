package backend.external

import java.time.LocalDateTime

import backend.external.Host.{Wikidata, Wikipedia}
import backend.external.extensions.{ExtendedLink, LinkExtension, SearchExtension}
import backend.recon._
import backend.recon.Reconcilable.SongExtractor
import common.RichJson._
import common.rich.RichT._
import common.rich.collections.RichTraversableOnce._
import common.rich.func.ToMoreMonadErrorOps
import controllers.UrlPathUtils
import javax.inject.Inject
import models.Song
import play.api.libs.json.{JsObject, Json, JsString}
import play.api.mvc.{InjectedController, Result}

import scala.concurrent.{ExecutionContext, Future}

import scalaz.std.FutureInstances
import scalaz.syntax.ToBindOps

class ExternalController @Inject()(ec: ExecutionContext, external: MbExternalLinksProvider)
    extends InjectedController
        with ToBindOps with ToMoreMonadErrorOps with FutureInstances {
  private implicit val iec: ExecutionContext = ec

  private type KVPair = (String, play.api.libs.json.Json.JsValueWrapper)
  private val hosts: Seq[Host] =
    Seq(Host.MusicBrainz, Host.Wikipedia, Host.Wikidata, Host.AllMusic, Host.Facebook, Host.LastFm, Host.RateYourMusic)

  private def canonizeHost(e: ExtendedLink[_]): String = {
    val isMissing = e.host.name.endsWith("?")
    val isNew = e.isNew
    val canonized = e.host.canonize.name
    if (isNew) canonized + "*" else if (isMissing) canonized + "?" else canonized
  }
  private def toJson(e: LinkExtension[_]): KVPair = e.name -> e.link.address
  private def toJson(e: ExtendedLink[_]): KVPair = e.host.name -> Json.obj(
    // This has to be canonized late, otherwise the "*" will be deleted.
    // TODO find a less hacky way to solve this
    "host" -> canonizeHost(e),
    "main" -> e.link.address,
    "extensions" -> Json.obj(e.extensions.map(toJson).toSeq: _*))
  private def toJson(e: Traversable[ExtendedLink[_]]): JsObject =
    e.filterAndSortBy(_.host.canonize, hosts)
        // Filter non-new Wikidata, because nobody cares about those.
        .filter(e => e.host.canonize != Wikidata || e.isNew)
        // Unmark new Wikipedia links, because MusicBrainz only uses Wikidata now.
        // Using mapIf messes up the link's type inferrence due to existential types.
        .map(e => if (e.host.canonize == Wikipedia) e.unmark else e)
        .map(toJson) |> Json.obj

  private def toDateString(l: LocalDateTime): String = f"${l.getDayOfMonth}%02d/${l.getMonthValue}%02d"

  private def toJson(e: TimestampedExtendedLinks[_]): JsObject =
    toJson(e.links).mapTo(_ + ("timestamp" -> JsString(e.timestamp |> toDateString)))
  private def toJsonOrError(f: Future[TimestampedExtendedLinks[_]]): Future[JsObject] =
    f.map(toJson).handleErrorFlat(e => Json.obj("error" -> e.getMessage))

  def get(path: String) = Action.async {
    getLinks(UrlPathUtils parseSong path)
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
    val song = UrlPathUtils parseSong path
    external.delete(song) >> getLinks(song)
  }
  def updateRecon(path: String) = Action.async {request =>
    val json = request.body.asJson.get
    def getReconId(s: String) = json ostr s map ReconID
    val song: Song = UrlPathUtils parseSong path
    external.updateRecon(song, artistReconId = getReconId("artist"), albumReconId = getReconId("album"))
        .>>(getLinks(song))
  }
}
