package backend.mb

import backend.OptionRetriever
import backend.recon.{Album, AlbumReconScorer, Artist, Reconciler, ReconID}
import com.google.inject.Inject
import play.api.libs.json.{JsObject, JsValue}

import scala.concurrent.ExecutionContext

import common.rich.func.BetterFutureInstances._
import common.rich.func.MoreSeqInstances._
import common.rich.func.ToTransableOps.toTransableOps
import scalaz.Scalaz.{doubleInstance, ToFunctorOps}
import scalaz.std.option.optionInstance
import scalaz.syntax.foldable.ToFoldableOps

import common.json.RichJson._
import common.rich.RichT._

private class MbAlbumReconciler @Inject() (
    ec: ExecutionContext,
    downloader: JsonDownloader,
    artistReconciler: OptionRetriever[Artist, ReconID],
    albumReconScorer: AlbumReconScorer,
) extends Reconciler[Album] {
  private implicit val iec: ExecutionContext = ec

  override def apply(a: Album) = artistReconciler(a.artist)
    .flatMapF(artistId => downloader("release-group/", "limit" -> "100", "artist" -> artistId.id))
    .subFlatMap(parse(_, a))

  private def album(js: JsObject, a: Artist) =
    Album(title = js.str("title"), year = js.str("first-release-date").take(4).toInt, artist = a)
  private def parse(js: JsValue, a: Album): Option[ReconID] = js
    .objects("release-groups")
    .filter(_.has("first-release-date"))
    .filter(_.ostr("primary-type").exists(Set("Album", "EP")))
    // TODO topByFilter?
    .fproduct(js => albumReconScorer(album(js, a.artist), a))
    .filter(_._2 >= 0.85)
    .maximumBy(_._2)
    .map(_._1)
    .map(_.str("id").thrush(ReconID.validateOrThrow))
    .<| {
      case None => scribe.debug(s"Could not reconcile album: <$a>")
      case _ => ()
    }
}
