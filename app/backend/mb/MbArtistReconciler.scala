package backend.mb

import java.time.{Clock, LocalDate, Year, YearMonth}

import backend.RichTime._
import backend.albums.NewAlbum.AlbumType
import backend.albums.NewAlbum.AlbumType.AlbumType
import backend.configs.StandaloneConfig
import backend.mb.JsonHelper._
import backend.mb.MbArtistReconciler.MbAlbumMetadata
import backend.recon._
import common.CompositeDateFormat
import common.RichJson._
import common.io.InternetTalker
import common.rich.RichFuture._
import common.rich.RichT._
import play.api.libs.json._

import scala.concurrent.Future
import scala.concurrent.duration._

class MbArtistReconciler(implicit it: InternetTalker) extends OnlineReconciler[Artist] {
  override def apply(a: Artist): Future[Option[ReconID]] =
    retry(() => getJson("artist/", ("query", a.name)), 5, 2 seconds)
        .map(_.objects("artists").find(_ has "type").get)
        .filterWith(_ str "score" equals "100", "could not find a 100 match")
        .map(_ ostr "id" map ReconID)

  private def parseDate(js: JsValue): LocalDate =
    MbArtistReconciler.compositeDateFormat.parse(js.str("first-release-date")).get.toLocalDate

  private def parseJson(json: JsObject): MbAlbumMetadata = {
    val date = parseDate(json)
    MbAlbumMetadata(title = json str "title",
      releaseDate = date,
      albumType = AlbumType.withName(json str "primary-type"),
      reconId = ReconID(json str "id"))
  }

  def getAlbumsMetadata(artistKey: ReconID): Future[Seq[MbAlbumMetadata]] =
    retry(() => getJson("release-group", ("artist", artistKey.id), ("limit", "100")), 10, 2 seconds)
        .map(_.objects("release-groups")
            .filter(_ has "first-release-date")
            .filter(_ ostr "primary-type" exists Set("Album", "EP", "Live"))
            .filter(_.array("secondary-types").value.isEmpty) // why?
            .sortBy(_ str "first-release-date")
            .map(parseJson))
}

object MbArtistReconciler {
  private val compositeDateFormat =
    CompositeDateFormat[LocalDate]("yyyy-MM-dd").orElse[YearMonth]("yyyy-MM").orElse[Year]("yyyy")

  def main(args: Array[String]) {
    implicit val c = StandaloneConfig
    val $ = new MbArtistReconciler
    $(Artist("Moonsorrow")).map(_.get).flatMap($.getAlbumsMetadata).get.log()
    System exit 0
  }

  case class MbAlbumMetadata(title: String, releaseDate: LocalDate, albumType: AlbumType, reconId: ReconID) {
    def isOut: Boolean = releaseDate.atStartOfDay < Clock.systemDefaultZone().toLocalDateTime
  }
}
