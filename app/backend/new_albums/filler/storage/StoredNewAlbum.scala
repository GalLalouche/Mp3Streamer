package backend.new_albums.filler.storage

import backend.new_albums.NewAlbum
import backend.new_albums.filler.NewAlbumRecon
import backend.recon.ReconID

// TODO revisit visibility — widened for server test access
case class StoredNewAlbum(
    na: NewAlbum,
    isRemoved: Boolean,
    isIgnored: Boolean,
) {
  def toAlbumRecon(reconID: ReconID) = NewAlbumRecon(na, reconID)
}
