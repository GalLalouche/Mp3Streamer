package backend.new_albums.filler

import backend.recon.{Album, Artist}
import rx.lang.scala.Observable

private trait ExistingAlbums {
  def artists: Observable[Artist]
  def albums: Artist => Set[Album]
  def allAlbums: Observable[Album] = artists.flatMapIterable(albums)
}
