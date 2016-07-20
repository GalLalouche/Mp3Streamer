package backend.mb

import backend.recon.{Artist, OnlineReconciler, ReconID}
import common.CompositeDateFormat
import common.Jsoner._
import common.RichFuture._
import play.api.libs.json._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class MusicBrainzRetriever(implicit ec: ExecutionContext) extends OnlineReconciler[Artist] with JsonHelper {
  override def apply(a: Artist): Future[Option[ReconID]] =
    retry(() => getJson("artist/", ("query", a.name)), 5, 2 seconds)
      .map(_ \ "artists")
      .map(_.as[JsArray].value
        .filter(_ has "type")
        .head)
      .filterWithMessage(webSearchResult => (webSearchResult \ "score").as[String] == "100", e => "could not find a 100 match")
      .map(_ \ "id" get)
      .map(_.as[String])
      .map(ReconID.apply)
      .map(Some.apply)

  private def parseAlbum(js: JsObject) = // some albums don't even have a date, probably because of reasons
    Try(CompositeDateFormat("yyyy-MM-dd", "yyyy-MM", "yyyy")
      .parse(js \ "first-release-date" get))
      .map(_.toLocalDate -> (js \ "title").get.as[String])
      .toOption

  private def getAlbumsAsArray(artistKey: String): Future[JsArray] =
    getJson("release-group", ("artist", artistKey), ("limit", "100"))
      .map(_ \ "release-groups" get)
      .map($ => new JsArray($
        .filter(_ has "first-release-date")
        .filter(_ has "primary-type")
        .filter(e => Set("Album", "EP", "Live").contains(e \ "primary-type" get))
        .filter(e => (e \ "secondary-types").get.as[JsArray].value.isEmpty)
        .toList
        .sortBy(e => (e \ "first-release-date").get.as[String])))
  def getAlbumsMetadata(key: ReconID) =
    getAlbumsAsArray(key.id)
      .map(_.value.map(_.as[JsObject])
        .flatMap(parseAlbum))
}
object MusicBrainzRetriever extends MusicBrainzRetriever()(ExecutionContext.Implicits.global) {
  def main(args: Array[String]) {
    import scala.concurrent.ExecutionContext.Implicits.global
    getJson("tags", "artist" -> "70248960-cb53-4ea4-943a-edb18f7d336f").get
  }
}
