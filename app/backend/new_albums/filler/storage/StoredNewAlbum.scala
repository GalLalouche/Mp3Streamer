package backend.new_albums.filler.storage

import backend.new_albums.NewAlbum
import backend.new_albums.filler.NewAlbumRecon
import backend.recon.ReconID

private[filler] case class StoredNewAlbum(
    na: NewAlbum,
    isRemoved: Boolean,
    isIgnored: Boolean,
) {
  def toAlbumRecon(reconID: ReconID) = NewAlbumRecon(na, reconID)
}
