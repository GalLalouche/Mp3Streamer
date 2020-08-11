package backend.mb

import java.time.{LocalDate, Year, YearMonth}

import backend.logging.Logger
import backend.mb.AlbumParser._
import backend.recon.ReconID
import javax.inject.Inject
import mains.fixer.StringFixer
import play.api.libs.json.{JsObject, JsValue}

import common.json.RichJson._
import common.rich.primitives.RichString._
import common.CompositeDateFormat

private class AlbumParser @Inject()(
    logger: Logger // TODO replace logging with ADT Result type
) {
  private def parseDate(js: JsValue): Option[LocalDate] = {
    val $ = js.ostr(ReleaseDate)
        .flatMap(DateFormatter.parse)
        .map(_.toLocalDate)
    if (js.has(ReleaseDate) && $.isEmpty)
      logger.warn(s"Could not parse $ReleaseDate from <$js>")
    $
  }

  def apply(json: JsObject): Option[MbAlbumMetadata] = for {
    date <- parseDate(json)
    albumType <- json.ostr("primary-type").flatMap(AlbumType.withNameOption)
    if ValidPrimaryTypes(albumType.entryName)
    // Secondary types includes compilations and other unwanted albums.
    if json.array("secondary-types").value.isEmpty
  } yield MbAlbumMetadata(
    title = fixQuotes(json str "title"),
    releaseDate = date,
    albumType = albumType,
    reconId = ReconID.validateOrThrow(json str "id"),
  )
}

private object AlbumParser {
  private val ReleaseDate = "first-release-date"
  private val ValidPrimaryTypes = Set("Album", "EP", "Live")
  private def fixQuotes(s: String): String =
    s.replaceAll(StringFixer.SpecialQuotes, "\"").replaceAll(StringFixer.SpecialApostrophes, "'")
  private val DateFormatter =
    CompositeDateFormat[LocalDate]("yyyy-MM-dd").orElse[YearMonth]("yyyy-MM").orElse[Year]("yyyy")
}
