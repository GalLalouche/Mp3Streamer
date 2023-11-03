package backend.albums.filler.storage

import backend.albums.filler.NewAlbumRecon
import backend.albums.NewAlbum
import backend.recon.ReconID

private[filler] case class StoredNewAlbum(
    na: NewAlbum,
    isRemoved: Boolean,
    isIgnored: Boolean,
) {
  def toAlbumRecon(reconID: ReconID) = NewAlbumRecon(na, reconID)
}
