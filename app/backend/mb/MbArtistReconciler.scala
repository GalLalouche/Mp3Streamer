package backend.mb

import java.time.{Clock, LocalDate, Year, YearMonth}

import backend.RichTime._
import backend.albums.NewAlbum.AlbumType
import backend.mb.MbArtistReconciler.MbAlbumMetadata
import backend.recon.{Artist, Reconciler, ReconID}
import backend.FutureOption
import common.CompositeDateFormat
import common.RichJson._
import common.rich.RichFuture
import common.rich.func.ToMoreMonadErrorOps
import javax.inject.Inject
import play.api.libs.json.{JsObject, JsValue}

import scala.Ordering.Implicits._
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.DurationInt

import scalaz.std.FutureInstances

class MbArtistReconciler @Inject()(
    ec: ExecutionContext,
    jsonHelper: JsonHelper,
) extends Reconciler[Artist]
    with FutureInstances with ToMoreMonadErrorOps {
  private implicit val iec: ExecutionContext = ec
  override def apply(a: Artist): FutureOption[ReconID] =
    jsonHelper.retry(() => jsonHelper.getJson("artist/", ("query", a.name)), 5, 2 seconds)
        .map(_.objects("artists").maxBy(_ int "score"))
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
  import backend.module.StandaloneModule
  import com.google.inject.Guice
  import RichFuture._
  import common.rich.RichT._
  import net.codingwell.scalaguice.InjectorExtensions._

  private val compositeDateFormat =
    CompositeDateFormat[LocalDate]("yyyy-MM-dd").orElse[YearMonth]("yyyy-MM").orElse[Year]("yyyy")

  def main(args: Array[String]) {
    val injector = Guice createInjector StandaloneModule
    implicit val ec: ExecutionContext = injector.instance[ExecutionContext]
    val $ = injector.instance[MbArtistReconciler]
    $(Artist("Moonsorrow")).map(_.get).flatMap($.getAlbumsMetadata).get.log()
    System exit 0
  }

  case class MbAlbumMetadata(title: String, releaseDate: LocalDate, albumType: AlbumType.AlbumType, reconId: ReconID) {
    def isOut: Boolean = releaseDate.atStartOfDay < Clock.systemDefaultZone().getLocalDateTime
  }
}
