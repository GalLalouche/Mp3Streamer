package backend.mb

import backend.{FutureOption, OptionRetriever}
import backend.recon.{Album, Artist, Reconciler, ReconID, ReconScorers}
import common.rich.RichT._
import common.RichJson._
import common.rich.func.RichOptionT._
import common.rich.func.ToMoreFoldableOps
import javax.inject.Inject
import play.api.libs.json.{JsObject, JsValue}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.DurationInt

import scalaz.std.{FutureInstances, OptionInstances}
import scalaz.OptionT

private class MbAlbumReconciler @Inject()(
    ec: ExecutionContext,
    jsonHelper: JsonHelper,
    artistReconciler: OptionRetriever[Artist, ReconID],
) extends Reconciler[Album]
    with ToMoreFoldableOps with OptionInstances with FutureInstances {
  private implicit val iec: ExecutionContext = ec
  private val scorer = ReconScorers.AlbumReconScorer
  private def toAlbum(js: JsObject, a: Artist) = Album(
    title = js str "title", year = js.str("first-release-date").take(4).toInt, artist = a)
  private def parse(js: JsValue, a: Album): Option[ReconID] = js.objects("release-groups")
      .filter(_ has "first-release-date")
      .filter(_ ostr "primary-type" exists Set("Album", "EP"))
      .find(0.9 <= _.mapTo(toAlbum(_, a.artist)).mapTo(scorer(_, a)))
      .map(_ str "id" mapTo ReconID)

  override def apply(a: Album): FutureOption[ReconID] =
    OptionT(artistReconciler(a.artist))
        .flatMapF(artistId => jsonHelper.retry(() =>
          jsonHelper.getJson(
            "release-group/", "limit" -> "100", "artist" -> artistId.id),
          times = 5,
          retryWait = 20 seconds,
        )).subFlatMap(parse(_, a))
        .run
}

object MbAlbumReconciler {
  import backend.module.StandaloneModule
  import com.google.inject.Guice
  import common.rich.RichFuture._
  import net.codingwell.scalaguice.InjectorExtensions._

  def main(args: Array[String]): Unit = {
    val injector = Guice createInjector StandaloneModule
    implicit val ec: ExecutionContext = injector.instance[ExecutionContext]
    val artistReconciler: OptionRetriever[Artist, ReconID] =
      ("6b335658-22c8-485d-93de-0bc29a1d0349" |> ReconID |> Some.apply |> Future.successful).const
    val $ = new MbAlbumReconciler(ec, injector.instance[JsonHelper], artistReconciler)
    val f = $(Album("Hell Bent for Leather", 1979, "Judas Priest" |> Artist.apply))
    f.get.log()
  }
}
