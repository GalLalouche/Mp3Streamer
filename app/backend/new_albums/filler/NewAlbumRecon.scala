package backend.new_albums.filler

import backend.new_albums.NewAlbum
import backend.recon.ReconID

private case class NewAlbumRecon(
    newAlbum: NewAlbum,
    reconId: ReconID,
    disambiguation: Option[String],
) {
  override def toString: String = disambiguation
    .fold(s"NewAlbumRecon($newAlbum, $reconId)")(d => s"NewAlbumRecon($newAlbum, $reconId, $d)")
}

private object NewAlbumRecon {
  def apply(newAlbum: NewAlbum, reconId: ReconID): NewAlbumRecon =
    apply(newAlbum, reconId, disambiguation = None)
}
