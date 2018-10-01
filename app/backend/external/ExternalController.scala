package backend.external

import backend.external.extensions.SearchExtension
import backend.recon._
import backend.recon.Reconcilable.SongExtractor
import common.RichJson._
import common.rich.RichT._
import controllers.UrlPathUtils
import javax.inject.Inject
import models.Song
import play.api.libs.json.Json
import play.api.mvc.{InjectedController, Result}

import scala.concurrent.{ExecutionContext, Future}

import scalaz.std.FutureInstances
import scalaz.syntax.ToBindOps

class ExternalController @Inject()(
    ec: ExecutionContext,
    external: MbExternalLinksProvider,
    jsonifier: ExternalJsonifier,
) extends InjectedController
    with ToBindOps with FutureInstances {
  private implicit val iec: ExecutionContext = ec

  def get(path: String) = Action.async {
    getLinks(UrlPathUtils parseSong path)
  }

  private def getLinks(song: Song): Future[Result] = {
    val links = external(song)
    val extendMissing = TimestampedExtendedLinks.links.modify(
      SearchExtension.extendMissing(ExternalJsonifier.Hosts, song.artist))
    val f = for {
      artistJson <- links.artistLinks.map(extendMissing) |> jsonifier.toJsonOrError
      albumJson <- links.albumLinks |> jsonifier.toJsonOrError
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
    val updatedRecon = UpdatedRecon.fromOptionals(getReconId("artist"), getReconId("album"))
    external.updateRecon(song)(updatedRecon) >> getLinks(song)
  }
}
