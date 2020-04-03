package backend.albums.filler

import backend.albums.NewAlbum
import backend.recon.ReconID

private case class NewAlbumRecon(newAlbum: NewAlbum, reconId: ReconID)
