package backend.mb

import backend.albums.NewAlbum.AlbumType
import backend.albums.NewAlbum.AlbumType.AlbumType
import backend.configs.StandaloneConfig
import backend.mb.JsonHelper._
import backend.mb.MbArtistReconciler.MbAlbumMetadata
import backend.recon._
import common.CompositeDateFormat
import common.RichJson._
import common.rich.RichFuture._
import common.rich.RichT._
import org.joda.time.LocalDate
import play.api.libs.json._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class MbArtistReconciler(implicit ec: ExecutionContext) extends OnlineReconciler[Artist] {
  override def apply(a: Artist): Future[Option[ReconID]] =
    retry(() => getJson("artist/", ("query", a.name)), 5, 2 seconds)
        .map(_.array("artists").find(_ has "type").get)
        .filterWith(_ str "score" equals "100", "could not find a 100 match")
        .map(_ ostr "id" map ReconID)

  private def parseDate(js: JsValue): Option[LocalDate] =
    MbArtistReconciler.compositeDateFormat.parse(js.str("first-release-date"))
        .map(_.toLocalDate)

  def getAlbumsMetadata(artistKey: ReconID): Future[Seq[MbAlbumMetadata]] =
    retry(() => getJson("release-group", ("artist", artistKey.id), ("limit", "100")), 10, 2 seconds)
        .map(_.array("release-groups")
            .filter(_ has "first-release-date")
            .filter(_ ostr "primary-type" exists Set("Album", "EP", "Live"))
            .filter(_.array("secondary-types").value.isEmpty) // why?
            .sortBy(_ str "first-release-date")
            .mapTo(JsArray))
        .map(_.flatMap {json =>
          for (date <- parseDate(json)) yield {
            MbAlbumMetadata(title = json str "title",
              releaseDate = date,
              albumType = AlbumType.withName(json str "primary-type"),
              reconId = ReconID(json str "id"))
          }
        })
}

object MbArtistReconciler {
  private val compositeDateFormat = CompositeDateFormat("yyyy-MM-dd", "yyyy-MM", "yyyy")

  def main(args: Array[String]) {
    implicit val c = StandaloneConfig
    val $ = new MbArtistReconciler
    $(Artist("Moonsorrow")).map(_.get).flatMap($.getAlbumsMetadata).get.log()
    System exit 0
  }

  case class MbAlbumMetadata(title: String, releaseDate: LocalDate, albumType: AlbumType, reconId: ReconID) {
    def isOut = releaseDate.toDateTimeAtCurrentTime isBefore System.currentTimeMillis
  }
}
