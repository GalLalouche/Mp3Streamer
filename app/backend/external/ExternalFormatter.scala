package backend.external

import java.io.File

import backend.external.extensions.SearchExtension
import backend.recon.Reconcilable.SongExtractor
import backend.recon.ReconID
import com.google.inject.Inject
import models.{IOSong, Song}
import play.api.libs.json.{Json, JsValue}

import scala.concurrent.{ExecutionContext, Future}

import common.rich.func.BetterFutureInstances._
import scalaz.syntax.bind.ToBindOps

import common.json.RichJson._
import common.rich.RichT._

class ExternalFormatter @Inject() (
    ec: ExecutionContext,
    external: MbExternalLinksProvider,
    jsonifier: ExternalJsonifier,
) {
  private implicit val iec: ExecutionContext = ec

  private def getLinks(song: Song): Future[JsValue] = {
    val links = external(song)
    val extendMissingArtist = TimestampedExtendedLinks.links.modify(
      SearchExtension.extendMissing(ExternalJsonifier.Hosts, song.artist),
    )
    val extendMissingAlbum = TimestampedExtendedLinks.links.modify(
      SearchExtension.extendMissing(Set(Host.RateYourMusic), song.release),
    )
    for {
      artistJson <- links.artistLinks.map(extendMissingArtist) |> jsonifier.toJsonOrError
      albumJson <- links.albumLinks.map(extendMissingAlbum) |> jsonifier.toJsonOrError
    } yield Json.obj("Artist" -> artistJson, "Album" -> albumJson)
  }

  def get(path: String): Future[JsValue] = getLinks(IOSong.read(new File(path)))

  def refreshArtist(path: String): Future[JsValue] = refresh(path, external.deleteArtist)
  def refreshAlbum(path: String): Future[JsValue] = refresh(path, external.deleteAlbum)

  private def refresh(path: String, deleteAction: Song => Future[_]): Future[JsValue] = {
    val song = IOSong.read(new File(path))
    deleteAction(song) >> getLinks(song)
  }
  def updateRecon(path: String, json: JsValue): Future[JsValue] = {
    def getReconId(s: String) = json.ostr(s).map(ReconID.validateOrThrow)
    val song: Song = IOSong.read(new File(path))
    val updatedRecon = UpdatedRecon.fromOptionals(getReconId("artist"), getReconId("album"))
    external.updateRecon(song)(updatedRecon) >> getLinks(song)
  }
}
