package backend.mb

import backend.Retriever
import backend.configs.StandaloneConfig
import backend.mb.JsonHelper._
import backend.recon._
import common.RichJson._
import common.io.InternetTalker
import common.rich.RichT._
import play.api.libs.json._

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class MbAlbumReconciler(artistReconciler: Retriever[Artist, ReconID])(implicit it: InternetTalker)
    extends OnlineReconciler[Album] {
  private val scorer = ReconScorers.AlbumReconScorer
  private def toAlbum(js: JsObject, a: Artist) = Album(js str "title", js str "first-release-date" take 4 toInt, a)
  private def parse(js: JsValue, a: Album): Option[ReconID] = js.objects("release-groups")
      .filter(_ has "first-release-date")
      .filter(_ ostr "primary-type" exists Set("Album", "EP"))
      .find(0.9 <= _.mapTo(toAlbum(_, a.artist)).mapTo(scorer(_, a)))
      .map(_ str "id" mapTo ReconID)

  override def apply(a: Album): Future[Option[ReconID]] =
    artistReconciler(a.artist)
        .flatMap(artistId => retry(() =>
          getJson("release-group/", "limit" -> "100", "artist" -> artistId.id), 5, 20 seconds))
        .map(parse(_, a))
}

object MbAlbumReconciler {
  def main(args: Array[String]) {
    implicit val c = StandaloneConfig
    val $ = new MbAlbumReconciler(_ => "6b335658-22c8-485d-93de-0bc29a1d0349" |> ReconID |> Future.successful)
    val f = $(Album("Hell Bent for Leather", 1979, "Judas Priest" |> Artist.apply))
    Await.result(f, 10 seconds).log()
  }
}
