package backend.mb

import java.time.{Clock, LocalDate}

import backend.albums.AlbumType
import backend.recon.ReconID
import backend.RichTime._

import scala.Ordering.Implicits._

case class MbAlbumMetadata(title: String, releaseDate: LocalDate, albumType: AlbumType, reconId: ReconID) {
  def isReleased: Boolean = releaseDate.atStartOfDay < Clock.systemDefaultZone().getLocalDateTime
}
