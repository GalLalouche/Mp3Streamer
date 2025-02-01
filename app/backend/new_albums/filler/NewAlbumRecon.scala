package backend.new_albums.filler

import backend.new_albums.NewAlbum
import backend.recon.ReconID

private case class NewAlbumRecon(newAlbum: NewAlbum, reconId: ReconID)
