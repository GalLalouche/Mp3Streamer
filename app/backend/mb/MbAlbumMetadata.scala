package backend.mb

import java.time.{Clock, LocalDate}
import scala.Ordering.Implicits._

import backend.recon.ReconID
import common.rich.primitives.RichString._
import common.rich.RichTime.{OrderingLocalDateTime, RichClock}
import mains.fixer.StringFixer

private[backend] case class MbAlbumMetadata(
    title: String,
    releaseDate: LocalDate,
    albumType: AlbumType,
    reconId: ReconID,
) {
  assert(title.doesNotContainMatch(StringFixer.SpecialQuotes))
  assert(title.doesNotContainMatch(StringFixer.SpecialApostrophes))
  def isReleased: Boolean = releaseDate.atStartOfDay < Clock.systemDefaultZone().getLocalDateTime
}
