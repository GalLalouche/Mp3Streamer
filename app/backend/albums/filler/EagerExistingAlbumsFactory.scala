package backend.albums.filler

import java.time.Clock

import backend.logging.Logger
import backend.recon.{Artist, StringReconScorer}
import javax.inject.Inject
import models.MusicFinder

import common.io.DirectoryRef
import common.rich.RichT._

private class EagerExistingAlbumsFactory @Inject()(
    mf: MusicFinder,
    clock: Clock,
    logger: Logger,
    stringReconScorer: StringReconScorer,
) {
  def from(albums: Seq[DirectoryRef]) = new EagerExistingAlbums(
    albums
        .map(ExistingAlbums.toAlbum(mf))
        .groupBy(_.artist.normalized)
        .mapValues(_.toSet)
        .view.force,
    clock,
    logger,
    stringReconScorer,
  )

  def singleArtist(artist: Artist): EagerExistingAlbums = {
    val artistDir = mf.findArtistDir(artist.name).get
    from(artistDir.dirs.mapIf(_.isEmpty).to(Vector(artistDir)))
  }
}