package backend.albums.filler

import javax.inject.Inject

import backend.recon.{Artist, ReconcilableFactory}
import common.io.DirectoryRef
import common.rich.RichT._
import models.MusicFinder

private class EagerExistingAlbumsFactory @Inject() (
    mf: MusicFinder,
    reconcilableFactory: ReconcilableFactory,
) {
  def from(albums: Seq[DirectoryRef]) = new EagerExistingAlbums(
    albums
      .map(reconcilableFactory.toAlbum(_).get)
      .groupBy(_.artist.normalized)
      .mapValues(_.toSet)
      .view
      .force,
  )

  def singleArtist(artist: Artist): EagerExistingAlbums = {
    val artistDir = mf.findArtistDir(artist.name).get
    from(artistDir.dirs.mapIf(_.isEmpty).to(Vector(artistDir)))
  }
}
