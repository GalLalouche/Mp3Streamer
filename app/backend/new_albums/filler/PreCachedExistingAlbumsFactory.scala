package backend.new_albums.filler

import backend.recon.{Artist, ReconcilableFactory}
import com.google.inject.Inject
import musicfinder.MusicFinder

import common.io.DirectoryRef
import common.rich.RichT._

private class PreCachedExistingAlbumsFactory @Inject() (
    mf: MusicFinder,
    reconcilableFactory: ReconcilableFactory,
) {
  def from(albums: Seq[DirectoryRef]) = new PreCachedExistingAlbums(
    albums
      .map(reconcilableFactory.toAlbum(_).get)
      .groupBy(_.artist)
      .mapValues(_.toSet)
      .view
      .force,
  )

  def singleArtist(artist: Artist): PreCachedExistingAlbums = {
    val artistDir = mf.findArtistDir(artist).get
    from(artistDir.dirs.mapIf(_.isEmpty).to(Vector(artistDir)))
  }
}
