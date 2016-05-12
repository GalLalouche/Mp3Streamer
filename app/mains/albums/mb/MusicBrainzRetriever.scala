package mains.albums.mb


import common.CompositeDateFormat
import common.Jsoner._
import common.RichFuture._
import mains.albums.{ID, OnlineReconciler}
import play.api.libs.json._

import scala.concurrent.duration._
import common.rich.collections.RichTraversable._
import org.joda.time.LocalDate

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class MusicBrainzRetriever(implicit ec: ExecutionContext) extends OnlineReconciler with JsonHelper {
  override def recon(artistName: String): Future[ID] =
    retry(() => getJson("artist/", ("query", artistName)), 5, 2 seconds)
        .map(_ \ "artists")
        .map(_.as[JsArray].value
            .filter(_ has "type")
            .head)
        .filterWithMessage(webSearchResult => (webSearchResult \ "score").as[String] == "100", e => "could not find a 100 match")
        .map(_ \ "id" get)

  private def parseAlbum(js: JsObject) =
    Try(CompositeDateFormat("yyyy-MM-dd", "yyyy-MM", "yyyy") // some albums don't even have a date, probably because of reasons
        .parse(js \ "first-release-date" get))
        .map(_.toLocalDate -> (js \ "title").get.as[String])
        .toOption

  private def getAlbumsAsArray(artistKey: ID): Future[JsArray] =
    getJson("release-group", ("artist", artistKey), ("limit", "100"))
        .map(_ \ "release-groups" get)
        .map($ => new JsArray($
            .filter(_ has "first-release-date")
            .filter(_ has "primary-type")
            .filter(e => Set("Album", "EP", "Live").contains(e \ "primary-type" get))
            .filter(e => (e \ "secondary-types").get.as[JsArray].value.isEmpty)
            .toList
            .sortBy(e => (e \ "first-release-date").get.as[String])))
  override def getAlbumsMetadata(key: ID) =
    getAlbumsAsArray(key)
        .map(_.value.map(_.as[JsObject])
            .mapDefined(parseAlbum).toSeq)
}
object MusicBrainzRetriever extends MusicBrainzRetriever()(ExecutionContext.Implicits.global)
