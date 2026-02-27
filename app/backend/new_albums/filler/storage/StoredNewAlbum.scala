package backend.new_albums.filler.storage

import backend.new_albums.NewAlbum

case class StoredNewAlbum(
    na: NewAlbum,
    isRemoved: Boolean,
    isIgnored: Boolean,
)
