package backend.mb

import backend.mb.JsonHelper._
import backend.recon.{Album, Artist, OnlineReconciler, ReconID}
import backend.storage.Retriever
import common.rich.RichT._
import play.api.libs.json._

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

class MbAlbumReconciler(artistReconciler: Retriever[Artist, ReconID])(implicit ec: ExecutionContext) extends OnlineReconciler[Album] {
  private def parse(js: JsValue, a: Album): Option[ReconID] =
    js.log(Json.prettyPrint).\("release-groups").as[JsArray].value
        .filter(e => e.has("primary-type"))
        .filter(e => (e \ "primary-type").as[String].mapTo(t => t == "Album" || t == "EP"))
        .find(e => e.\("title").as[String].toLowerCase == a.title.toLowerCase)
        .map(_.\("id").get.as[String]).map(ReconID)
  
  override def apply(a: Album): Future[Option[ReconID]] =
    artistReconciler(a.artist)
        .flatMap(artistId => retry(() => getJson("release-group/", "artist" -> artistId.id), 5, 2 seconds))
        .map(parse(_, a))
}

object MbAlbumReconciler {
  def main(args: Array[String]) {
    val $ = new MbAlbumReconciler(e => "5b9890b9-a2e8-4768-ad70-9f28bb81fc00" |> ReconID |> Future.successful)(ExecutionContext.Implicits.global)
    val f = $(Album("Now That You're Leaving", "Foobar" |> Artist))
    Await.result(f, 10 seconds).log()
  }
}
