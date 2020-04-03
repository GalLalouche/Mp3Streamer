package backend.mb

import java.time.{Clock, LocalDate}

import backend.albums.AlbumType
import backend.recon.ReconID
import backend.RichTime._
import mains.fixer.StringFixer

import scala.Ordering.Implicits._

import common.rich.primitives.RichBoolean._

case class MbAlbumMetadata(title: String, releaseDate: LocalDate, albumType: AlbumType, reconId: ReconID) {
  assert(StringFixer.SpecialQuotes.matcher(title).find().isFalse)
  assert(StringFixer.SpecialApostrophes.matcher(title).find().isFalse)
  def isReleased: Boolean = releaseDate.atStartOfDay < Clock.systemDefaultZone().getLocalDateTime
}
