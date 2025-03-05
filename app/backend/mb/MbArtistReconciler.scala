package backend.mb

import com.google.inject.Inject

import backend.recon.{Artist, Reconciler, ReconID}

import scala.concurrent.{ExecutionContext, Future}

import common.rich.func.BetterFutureInstances._
import common.rich.func.ToMoreMonadErrorOps._
import scalaz.OptionT

import common.json.RichJson._

private[backend] class MbArtistReconciler @Inject() (
    ec: ExecutionContext,
    downloader: JsonDownloader,
) extends Reconciler[Artist] {
  private implicit val iec: ExecutionContext = ec
  override def apply(a: Artist) = OptionT {
    downloader("artist/", "query" -> a.name)
      .map(_.objects("artists"))
      .filterWithMessage(_.nonEmpty, s"Found no artists for <$a>")
      .map(_.maxBy(_.int("score")))
      .filterWithMessage(_.int("score") == 100, "could not find a 100 match")
      .map(_.ostr("id").map(ReconID.validateOrThrow))
  }

  // TODO Extract this to another module, this shouldn't be here
  def getAlbumsMetadata(artistKey: ReconID): Future[Seq[MbAlbumMetadata]] =
    downloader("release-group", "artist" -> artistKey.id).map(AlbumParser.releaseGroups)
}
