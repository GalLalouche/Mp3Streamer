package backend.mb

import java.time.{Clock, LocalDate, Year, YearMonth}

import backend.RichTime._
import backend.albums.NewAlbum.AlbumType
import backend.configs.{Configuration, StandaloneConfig}
import backend.mb.MbArtistReconciler.MbAlbumMetadata
import backend.recon._
import common.CompositeDateFormat
import common.RichJson._
import common.rich.RichFuture._
import common.rich.RichT._
import common.rich.func.ToMoreMonadErrorOps
import net.codingwell.scalaguice.InjectorExtensions._
import play.api.libs.json._

import scala.Ordering.Implicits._
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

import scalaz.std.FutureInstances

class MbArtistReconciler(implicit c: Configuration) extends OnlineReconciler[Artist]
    with FutureInstances with ToMoreMonadErrorOps {
  private implicit val ec: ExecutionContext = c.injector.instance[ExecutionContext]
  private val jsonHelper = c.injector.instance[JsonHelper]
  override def apply(a: Artist): Future[Option[ReconID]] =
    jsonHelper.retry(() => jsonHelper.getJson("artist/", ("query", a.name)), 5, 2 seconds)
        .map(_.objects("artists").find(_ has "type").get)
        .filterWithMessage(_.int("score") == 100, "could not find a 100 match")
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
    jsonHelper.retry(() => jsonHelper.getJson("release-group", ("artist", artistKey.id), ("limit", "100")), 10, 2 seconds)
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
    implicit val c: Configuration = StandaloneConfig
    implicit val ec: ExecutionContext = c.injector.instance[ExecutionContext]
    val $ = new MbArtistReconciler
    $(Artist("Moonsorrow")).map(_.get).flatMap($.getAlbumsMetadata).get.log()
    System exit 0
  }

  case class MbAlbumMetadata(title: String, releaseDate: LocalDate, albumType: AlbumType.AlbumType, reconId: ReconID) {
    def isOut: Boolean = releaseDate.atStartOfDay < Clock.systemDefaultZone().getLocalDateTime
  }
}
