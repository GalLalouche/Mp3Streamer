package backend.new_albums.filler

import backend.new_albums.NewAlbum
import backend.recon.ReconID

// TODO revisit visibility - widened for server test access
case class NewAlbumRecon(
    newAlbum: NewAlbum,
    reconId: ReconID,
    disambiguation: Option[String],
) {
  override def toString: String = disambiguation
    .fold(s"NewAlbumRecon($newAlbum, $reconId)")(d => s"NewAlbumRecon($newAlbum, $reconId, $d)")
}

object NewAlbumRecon {
  def apply(newAlbum: NewAlbum, reconId: ReconID): NewAlbumRecon =
    apply(newAlbum, reconId, disambiguation = None)
}
