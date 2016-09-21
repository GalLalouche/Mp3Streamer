package backend.mb

import backend.configs.StandaloneConfig
import backend.mb.JsonHelper._
import backend.recon.{Artist, OnlineReconciler, ReconID}
import common.CompositeDateFormat
import common.RichJson._
import common.rich.RichFuture._
import common.rich.RichT._
import play.api.libs.json._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class MbArtistReconciler(implicit ec: ExecutionContext) extends OnlineReconciler[Artist] {
  override def apply(a: Artist): Future[Option[ReconID]] =
    retry(() => getJson("artist/", ("query", a.name)), 5, 2 seconds)
        .map(_.array("artists").find(_ has "type").get)
        .filterWith(_ str "score" equals "100", "could not find a 100 match")
        .map(_ ostr "id" map ReconID)

  private def parseAlbum(js: JsValue) = // some albums don't even have a date, probably because of reasons
    Try(CompositeDateFormat("yyyy-MM-dd", "yyyy-MM", "yyyy").parse(js str "first-release-date"))
        .map(_.toLocalDate -> (js str "title"))
        .toOption

  private def getAlbumsAsArray(artistKey: String): Future[JsArray] =
    getJson("release-group", ("artist", artistKey), ("limit", "100"))
        .map(_.array("release-groups")
            .filter(_ has "first-release-date")
            .filter(_ ostr "primary-type" exists Set("Album", "EP", "Live"))
            .filter(_.array("secondary-types").value.isEmpty) // why?
            .sortBy(_ str "first-release-date")
            .mapTo(JsArray))

  def getAlbumsMetadata(key: ReconID) = getAlbumsAsArray(key.id).map(_ flatMap parseAlbum)
}

object MbArtistReconciler {
  def main(args: Array[String]) {
    implicit val c = StandaloneConfig
    val $ = new MbArtistReconciler
    $(Artist("Moonsorrow")).map(_.get).flatMap($.getAlbumsMetadata).get.log()
    System exit 0
  }
}
