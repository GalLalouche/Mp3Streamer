package backend.mb

import backend.recon.{Artist, Reconciler, ReconID}
import com.google.inject.Inject

import scala.concurrent.ExecutionContext

import cats.data.OptionT
import common.rich.func.kats.ToMoreMonadErrorOps._

import common.json.RichJson._

private[backend] class ArtistReconciler @Inject() (
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
}
