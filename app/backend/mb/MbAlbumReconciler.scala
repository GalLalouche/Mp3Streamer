package backend.mb

import backend.Retriever
import backend.recon.{Album, Artist, Reconciler, ReconID, ReconScorers}
import com.google.inject.assistedinject.Assisted
import common.rich.RichT._
import common.RichJson._
import javax.inject.Inject
import play.api.libs.json.{JsObject, JsValue}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.DurationInt

private class MbAlbumReconciler @Inject()(
    ec: ExecutionContext,
    jsonHelper: JsonHelper,
    @Assisted artistReconciler: Retriever[Artist, ReconID],
) extends Reconciler[Album] {
  private implicit val iec: ExecutionContext = ec
  private val scorer = ReconScorers.AlbumReconScorer
  private def toAlbum(js: JsObject, a: Artist) = Album(
    title = js str "title", year = js.str("first-release-date").take(4).toInt, artist = a)
  private def parse(js: JsValue, a: Album): Option[ReconID] = js.objects("release-groups")
      .filter(_ has "first-release-date")
      .filter(_ ostr "primary-type" exists Set("Album", "EP"))
      .find(0.9 <= _.mapTo(toAlbum(_, a.artist)).mapTo(scorer(_, a)))
      .map(_ str "id" mapTo ReconID)
  override def apply(a: Album): Future[Option[ReconID]] = {
    artistReconciler(a.artist)
        .flatMap(artistId => jsonHelper.retry(() =>
          jsonHelper.getJson("release-group/", "limit" -> "100", "artist" -> artistId.id), 5, 20 seconds))
        .map(parse(_, a))
  }
}

object MbAlbumReconciler {
  import backend.configs.StandaloneModule
  import com.google.inject.Guice
  import common.rich.RichFuture._
  import net.codingwell.scalaguice.InjectorExtensions._

  def main(args: Array[String]): Unit = {
    val injector = Guice createInjector StandaloneModule
    implicit val ec: ExecutionContext = injector.instance[ExecutionContext]
    val $ = injector.instance[AlbumReconcilerFactory].apply(_ => "6b335658-22c8-485d-93de-0bc29a1d0349" |> ReconID |> Future.successful)
    val f = $(Album("Hell Bent for Leather", 1979, "Judas Priest" |> Artist.apply))
    f.get.log()
  }
}


