package backend.mb

import backend.mb.JsonHelper._
import backend.recon._
import backend.storage.Retriever
import common.rich.RichT._
import play.api.libs.json._
import common.Jsoner._

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

class MbAlbumReconciler(artistReconciler: Retriever[Artist, ReconID])(implicit ec: ExecutionContext) extends OnlineReconciler[Album] {


  private val scorer = ReconScorers.AlbumReconScorer
  private def toAlbum(js: JsObject, artistName: String): Album = {
    Album(js \ "title" get, js.\("first-release-date").as[String].take(4).toInt, Artist(artistName))
  }
  private def parse(js: JsValue, a: Album): Option[ReconID] =
    js.\("release-groups").as[JsArray].value
        .filter(e => e.has("primary-type"))
        .filter(e => (e \ "primary-type").as[String].mapTo(t => t == "Album" || t == "EP"))
        .find(0.95 < _.mapTo(toAlbum(_, a.artist.name)).mapTo(scorer(_, a)))
        .map(_.\("id").get.as[String]).map(ReconID)

  override def apply(a: Album): Future[Option[ReconID]] =
    artistReconciler(a.artist)
        .flatMap(artistId => retry(() =>
          getJson("release-group/", "limit" -> "100", "artist" -> artistId.id), 5, 2 seconds))
        .map(parse(_, a))
}

object MbAlbumReconciler {
  def main(args: Array[String]) {
    val $ = new MbAlbumReconciler(e => "98fb7792-01fa-4ed1-a15d-20077a47210f" |> ReconID |> Future.successful)(ExecutionContext.Implicits.global)
    val f = $(Album("Verisakeet", 2005, "Moonsorrow" |> Artist))
    Await.result(f, 10 seconds).log()
  }
}
