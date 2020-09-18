package backend.albums.filler

import backend.albums.NewAlbum
import backend.recon.{Album, Artist, StringReconScorer}
import models.MusicFinder

import common.io.DirectoryRef
import common.rich.collections.RichTraversableOnce._
import common.rich.RichT._

private class EagerExistingAlbums private(override val albums: Map[Artist, Set[Album]])
    extends ExistingAlbums {
  override def artists: Iterable[Artist] = albums.keys

  def removeExistingAlbums(artist: Artist, allAlbums: Seq[NewAlbum]): Seq[NewAlbum] = {
    // TODO is released
    val albumTitles = albums(artist.normalized).map(_.title)
    allAlbums.filter(a => albumTitles.fornone(StringReconScorer(_, a.title) > 0.95))
  }
}

private object EagerExistingAlbums {
  def from(albums: Seq[DirectoryRef], mf: MusicFinder) = {
    new EagerExistingAlbums(albums
        .map(ExistingAlbums.toAlbum(mf))
        .groupBy(_.artist.normalized)
        .mapValues(_.toSet)
        .view.force
    )
  }

  def singleArtist(artist: Artist, mf: MusicFinder): EagerExistingAlbums = {
    val artistDir = mf.findArtistDir(artist.name).get
    EagerExistingAlbums.from(
      artistDir.dirs.mapIf(_.isEmpty).to(Vector(artistDir)),
      mf,
    )
  }
}
