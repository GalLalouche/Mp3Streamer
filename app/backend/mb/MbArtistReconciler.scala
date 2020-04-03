package backend.mb

import java.time.{LocalDate, Year, YearMonth}
import java.util.regex.Pattern

import backend.albums.AlbumType
import backend.recon.{Artist, Reconciler, ReconID}
import backend.FutureOption
import javax.inject.Inject
import mains.fixer.StringFixer
import play.api.libs.json.{JsObject, JsValue}

import scala.concurrent.{ExecutionContext, Future}

import scalaz.std.scalaFuture.futureInstance
import common.rich.func.ToMoreMonadErrorOps._

import common.rich.primitives.RichString._
import common.CompositeDateFormat
import common.json.RichJson._

class MbArtistReconciler @Inject()(
    ec: ExecutionContext,
    downloader: JsonDownloader,
) extends Reconciler[Artist] {
  private implicit val iec: ExecutionContext = ec
  override def apply(a: Artist): FutureOption[ReconID] =
    downloader("artist/", "query" -> a.name)
        .map(_.objects("artists").maxBy(_ int "score"))
        .filterWithMessage(_.int("score") == 100, "could not find a 100 match")
        .map(_ ostr "id" map ReconID)

  private def parseDate(js: JsValue): LocalDate =
    MbArtistReconciler.DateFormatter.parse(js.str("first-release-date")).get.toLocalDate

  private def parseJson(json: JsObject) = MbAlbumMetadata(
    title = MbArtistReconciler.fixQuotes(json str "title"),
    releaseDate = parseDate(json),
    albumType = AlbumType.withName(json str "primary-type"),
    reconId = ReconID(json str "id"),
  )

  def getAlbumsMetadata(artistKey: ReconID): Future[Seq[MbAlbumMetadata]] =
    downloader("release-group", "artist" -> artistKey.id, "limit" -> "100")
        .map(_.objects("release-groups")
            .filter(_ has "first-release-date")
            .filter(_ ostr "primary-type" exists Set("Album", "EP", "Live"))
            .filter(_.array("secondary-types").value.isEmpty) // why?
            .sortBy(_ str "first-release-date")
            .map(parseJson)
        )
}

object MbArtistReconciler {
  private def fixQuotes(s: String): String =
    s.replaceAll(StringFixer.SpecialQuotes, "\"").replaceAll(StringFixer.SpecialApostrophes, "'")
  private val DateFormatter =
    CompositeDateFormat[LocalDate]("yyyy-MM-dd").orElse[YearMonth]("yyyy-MM").orElse[Year]("yyyy")
}
