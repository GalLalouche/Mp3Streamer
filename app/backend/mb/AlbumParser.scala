package backend.mb

import java.time.{LocalDate, Year, YearMonth}

import backend.logging.Logger
import backend.mb.AlbumParser._
import backend.recon.{Artist, ReconID}
import javax.inject.Inject
import mains.fixer.StringFixer
import play.api.libs.json.{JsObject, JsValue}

import common.json.RichJson._
import common.rich.RichTime.OrderingLocalDate
import common.rich.collections.RichTraversableOnce._
import common.rich.primitives.RichString._
import common.CompositeDateFormat
import common.rich.RichT.richT

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

  def parseReleaseGroup(json: JsObject): Option[MbAlbumMetadata] = for {
    date <- parseDate(json)
    albumType <- json.ostr("primary-type").flatMap(AlbumType.withNameOption)
    if ValidPrimaryTypes(albumType.entryName)
    // Secondary types includes compilations, demos, and other unwanted albums. But sometimes they
    // contain live information instead of it being embedded in the album type...
    secondaryTypes = json.array("secondary-types").value.map(_.as[String].toLowerCase)
    if secondaryTypes.fornone(_ != "live")
  } yield {
    assert(secondaryTypes.singleOpt.forall(_ == "live"))
    MbAlbumMetadata(
      title = fixQuotes(json str "title"),
      releaseDate = date,
      albumType = if (secondaryTypes.nonEmpty) AlbumType.Live else albumType,
      reconId = ReconID.validateOrThrow(json str "id"),
    )
  }

  def artistCredits(json: JsObject): Seq[(Artist, ReconID)] = json
      .objects("artist-credit")
      .map(_
          ./("artist")
          .toTuple(
            _.str("name") |> Artist.apply,
            _.str("id") |> ReconID.validateOrThrow,
          ))

  // This is no longer used, but I'm keeping it in case I might need in the future.
  def releaseToReleaseGroups(js: JsValue): Seq[MbAlbumMetadata] = js.array("releases")
      .value
      .flatMap(_ / ("release-group") |> parseReleaseGroup)
      .groupBy(_.toTuple(_.title, _.albumType))
      .values
      .map(extractSingleRelease)
      .toVector

  def releaseGroups(js: JsValue): Seq[MbAlbumMetadata] =
    js.objects("release-groups").flatMap(parseReleaseGroup)
}

private object AlbumParser {
  private val ReleaseDate = "first-release-date"
  private val ValidPrimaryTypes = Set("Album", "EP", "Live")
  private def fixQuotes(s: String): String =
    s.replaceAll(StringFixer.SpecialQuotes, "\"").replaceAll(StringFixer.SpecialApostrophes, "'")
  private val DateFormatter =
    CompositeDateFormat[LocalDate]("yyyy-MM-dd").orElse[YearMonth]("yyyy-MM").orElse[Year]("yyyy")
  private def extractSingleRelease(releases: Iterable[MbAlbumMetadata]): MbAlbumMetadata = {
    val byDate = releases.groupBy(_.releaseDate)
    if (byDate.size > 1)
    // If there are multiple dates, choose the first one.
    // Note that there may be multiple releases with the same date though.
      return extractSingleRelease(byDate.minBy(_._1)._2)
    assert(releases.nonEmpty)
    if (releases.size == 1)
      return releases.head
    val freqs = releases.map(_.reconId).frequencies
    if (freqs.size == 1)
      return releases.head
    assert(freqs.size > 1)
    val reconCandidate = freqs.maxBy(_._2)._1
    assert(releases.hasSameValues(_.title))
    assert(releases.hasSameValues(_.albumType))
    require(releases.hasSameValues(_.releaseDate))
    releases.find(_.reconId == reconCandidate).get
  }
}
