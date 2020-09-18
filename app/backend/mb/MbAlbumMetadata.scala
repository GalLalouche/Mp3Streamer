package backend.mb

import java.time.{Clock, LocalDate}

import backend.recon.ReconID
import mains.fixer.StringFixer

import scala.Ordering.Implicits._

import common.rich.RichTime.{OrderingLocalDateTime, RichClock}
import common.rich.primitives.RichString._

private[backend] case class MbAlbumMetadata(title: String, releaseDate: LocalDate, albumType: AlbumType, reconId: ReconID) {
  assert(title doesNotContainMatch StringFixer.SpecialQuotes)
  assert(title doesNotContainMatch StringFixer.SpecialApostrophes)
  def isReleased: Boolean = releaseDate.atStartOfDay < Clock.systemDefaultZone().getLocalDateTime
}
