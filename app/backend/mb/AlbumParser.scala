package backend.mb

import java.time.{LocalDate, Year, YearMonth}

import backend.recon.{Artist, ReconID}
import mains.fixer.StringFixer
import play.api.libs.json.{JsObject, JsValue}

import scala.util.Try

import common.TryOption
import common.json.RichJson._
import common.rich.RichT.richT
import common.rich.collections.RichTraversableOnce._
import common.rich.primitives.RichString._

private object AlbumParser {
  def parseReleaseGroup(json: JsObject): TryOption[AlbumMetadata] = for {
    date <- parseDate(json)
    albumType <- TryOption
      .fromOption(json.ostr("primary-type"))
      .flatMapF(pt => Try(AlbumType.withName(pt)))
    if ValidPrimaryTypes(albumType.entryName)
    // Secondary types includes compilations, demos, and other unwanted albums. But sometimes they
    // contain live information instead of it being embedded in the album type...
    secondaryTypes = json.array("secondary-types").value.map(_.as[String].toLowerCase)
    if secondaryTypes.fornone(_ != "live")
  } yield {
    assert(secondaryTypes.singleOpt.forall(_ == "live"))
    AlbumMetadata(
      title = fixQuotes(json.str("title")),
      releaseDate = date,
      albumType =
        if (secondaryTypes.nonEmpty)
          if (albumType == AlbumType.EP) AlbumType.LiveEP else AlbumType.Live
        else
          albumType,
      reconId = ReconID.validateOrThrow(json.str("id")),
      disambiguation = json.str("disambiguation").optFilter(_.nonEmpty),
    )
  }

  private def parseDate(js: JsValue): TryOption[LocalDate] =
    TryOption.fromOption(js.ostr(ReleaseDate)).flatMap { dateStr =>
      TryOption
        .fromOption(DateFormatter.parse(dateStr).map(_.toLocalDate))
        .orElse(
          TryOption.Failure(
            new IllegalArgumentException(s"Could not parse $ReleaseDate from <$dateStr>"),
          ),
        )
    }

  def artistCredits(json: JsObject): Seq[(Artist, ReconID)] = json
    .objects("artist-credit")
    .map(
      _./("artist")
        .toTuple(
          _.str("name") |> Artist.apply,
          _.str("id") |> ReconID.validateOrThrow,
        ),
    )

  // This is no longer used, but I'm keeping it in case I might need in the future.
  def releaseToReleaseGroups(js: JsValue): Seq[AlbumMetadata] = js
    .array("releases")
    .value
    .flatMap(e => (e / "release-group").|>(parseReleaseGroup(_).toOption))
    .groupBy(_.toTuple(_.title, _.albumType, _.disambiguation))
    .values
    .map(extractSingleRelease)
    .toVector

  def releaseGroups(js: JsValue): Seq[Try[AlbumMetadata]] =
    js.objects("release-groups").map(parseReleaseGroup).flatMap(_.run)

  private val ReleaseDate = "first-release-date"
  private val ValidPrimaryTypes = Set("Album", "EP", "Live")
  private def fixQuotes(s: String): String =
    s.replaceAll(StringFixer.SpecialQuotes, "\"").replaceAll(StringFixer.SpecialApostrophes, "'")
  private val DateFormatter =
    CompositeDateFormat[LocalDate]("yyyy-MM-dd").orElse[YearMonth]("yyyy-MM").orElse[Year]("yyyy")
  private def extractSingleRelease(releases: Iterable[AlbumMetadata]): AlbumMetadata = {
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
