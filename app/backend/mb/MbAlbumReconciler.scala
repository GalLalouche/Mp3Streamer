package backend.mb

import backend.recon.{Album, Artist, OnlineReconciler, ReconID}
import common.rich.RichT._
import play.api.libs.json._

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

class MbAlbumReconciler(artistReconciler: Artist => Future[ReconID])(implicit ec: ExecutionContext) extends OnlineReconciler[Album] with JsonHelper {
  private def parse(js: JsValue, a: Album): Option[ReconID] = {
    js.\("release-groups").as[JsArray].value
      .filter(e => (e \ "primary-type").as[String] == "Album")
      .filter(e => e.\("title").as[String].toLowerCase == a.title.toLowerCase)
      .headOption.map(_.\("id").get.as[String]).map(ReconID)
  }

  override def apply(a: Album): Future[Option[ReconID]] =
    artistReconciler(a.artist)
      .flatMap(artistId => retry(() => getJson("release-group/", "query" -> a.title, "artist" -> artistId.id), 5, 2 seconds))
      .map(parse(_, a))
}

object MbAlbumReconciler {
  def main(args: Array[String]) {
    val $ = new MbAlbumReconciler(e => "6318e724-7e6b-4e41-a35b-080065077c80" |> ReconID |> Future.successful)(ExecutionContext.Implicits.global);
    val f = $(Album("Fortress", "Foobar" |> Artist))
    Await.result(f, 10 seconds).log()
  }
}
