package backend.external

import backend.external.extensions.SearchExtension
import backend.recon.Reconcilable.SongExtractor
import backend.recon.ReconID
import controllers.UrlPathUtils
import javax.inject.Inject
import models.Song
import play.api.libs.json.{Json, JsValue}

import scala.concurrent.{ExecutionContext, Future}

import scalaz.syntax.bind.ToBindOps
import common.rich.func.BetterFutureInstances._

import common.json.RichJson._
import common.rich.RichT._

private class ExternalFormatter @Inject() (
    ec: ExecutionContext,
    external: MbExternalLinksProvider,
    jsonifier: ExternalJsonifier,
    urlPathUtils: UrlPathUtils,
) {
  private implicit val iec: ExecutionContext = ec

  private def getLinks(song: Song): Future[JsValue] = {
    val links = external(song)
    val extendMissing = TimestampedExtendedLinks.links.modify(
      SearchExtension.extendMissing(ExternalJsonifier.Hosts, song.artist),
    )
    for {
      artistJson <- links.artistLinks.map(extendMissing) |> jsonifier.toJsonOrError
      albumJson <- links.albumLinks |> jsonifier.toJsonOrError
    } yield Json.obj("Artist links" -> artistJson, "Album links" -> albumJson)
  }

  def get(path: String): Future[JsValue] =
    getLinks(urlPathUtils.parseSong(path))

  def refresh(path: String): Future[JsValue] = {
    val song = urlPathUtils.parseSong(path)
    external.delete(song) >> getLinks(song)
  }
  def updateRecon(path: String, json: JsValue): Future[JsValue] = {
    def getReconId(s: String) = json.ostr(s).map(ReconID.validateOrThrow)
    val song: Song = urlPathUtils.parseSong(path)
    val updatedRecon = UpdatedRecon.fromOptionals(getReconId("artist"), getReconId("album"))
    external.updateRecon(song)(updatedRecon) >> getLinks(song)
  }
}
