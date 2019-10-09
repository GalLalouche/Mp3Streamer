package backend.mb

import backend.{FutureOption, OptionRetriever}
import backend.recon.{Album, Artist, Reconciler, ReconID, ReconScorers}
import javax.inject.Inject
import play.api.libs.json.{JsObject, JsValue}

import scala.concurrent.ExecutionContext

import scalaz.OptionT
import scalaz.std.scalaFuture.futureInstance
import common.rich.func.RichOptionT._

import common.rich.RichT._
import common.RichJson._

private class MbAlbumReconciler @Inject()(
    ec: ExecutionContext,
    downloader: JsonDownloader,
    artistReconciler: OptionRetriever[Artist, ReconID],
) extends Reconciler[Album] {
  private implicit val iec: ExecutionContext = ec
  private val scorer = ReconScorers.AlbumReconScorer

  override def apply(a: Album): FutureOption[ReconID] = OptionT(artistReconciler(a.artist))
      .flatMapF(artistId => downloader("release-group/", "limit" -> "100", "artist" -> artistId.id))
      .subFlatMap(parse(_, a))
      .run

  private def album(js: JsObject, a: Artist) = Album(
    title = js str "title", year = js.str("first-release-date").take(4).toInt, artist = a)
  private def parse(js: JsValue, a: Album): Option[ReconID] = js.objects("release-groups")
      .filter(_ has "first-release-date")
      .filter(_ ostr "primary-type" exists Set("Album", "EP"))
      .find(js => scorer(album(js, a.artist), a) >= 0.9)
      .map(_ str "id" mapTo ReconID)
}
