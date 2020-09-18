package backend.albums.filler

import java.time.Clock

import backend.albums.NewAlbum
import backend.recon.{Album, Artist, StringReconScorer}
import models.MusicFinder

import common.io.DirectoryRef
import common.rich.collections.RichTraversableOnce._
import common.rich.RichT._

private class EagerExistingAlbums private(
    override val albums: Map[Artist, Set[Album]],
    clock: Clock,
) extends ExistingAlbums {
  override def artists: Iterable[Artist] = albums.keys

  def removeExistingAndUnreleasedAlbums(
      artist: Artist, allAlbums: Seq[NewAlbum]
  ): Seq[NewAlbum] = {
    val albumTitles = albums(artist.normalized).map(_.title)
    allAlbums
        .filter(a => albumTitles.fornone(StringReconScorer(_, a.title) > 0.95))
        .filter(_.isReleased(clock))
  }
}

private object EagerExistingAlbums {
  def from(albums: Seq[DirectoryRef], mf: MusicFinder, clock: Clock) = new EagerExistingAlbums(
    albums
        .map(ExistingAlbums.toAlbum(mf))
        .groupBy(_.artist.normalized)
        .mapValues(_.toSet)
        .view.force,
    clock,
  )

  def singleArtist(artist: Artist, mf: MusicFinder, clock: Clock): EagerExistingAlbums = {
    val artistDir = mf.findArtistDir(artist.name).get
    EagerExistingAlbums.from(
      artistDir.dirs.mapIf(_.isEmpty).to(Vector(artistDir)),
      mf,
      clock
    )
  }
}
