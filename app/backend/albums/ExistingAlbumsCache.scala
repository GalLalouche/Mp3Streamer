package backend.albums

import backend.mb.MbAlbumMetadata
import backend.recon.{Album, Artist, StringReconScorer}
import models.MusicFinder

import common.rich.collections.RichTraversableOnce._
import common.rich.RichT._

private class ExistingAlbumsCache private(existingAlbums: Map[Artist, Set[Album]]) {
  import ExistingAlbumsCache._

  def artists: Iterable[Artist] = existingAlbums.keys

  def removeExistingAlbums(artist: Artist, albums: Seq[MbAlbumMetadata]): Seq[NewAlbumRecon] = for {
    album <- albums
    // TODO this should the album's ReconID.
    if album.isReleased && existingAlbums(canonicalize(artist))
        .map(_.title)
        .fornone(StringReconScorer(_, album.title) > 0.95)
  } yield NewAlbumRecon(NewAlbum.from(artist, album), album.reconId)
}

private object ExistingAlbumsCache {
  private def canonicalize(a: Artist): Artist = Artist(a.normalize)
  def from(albums: Seq[Album]) = new ExistingAlbumsCache(albums
      .groupBy(_.artist |> canonicalize)
      .mapValues(_.toSet)
      .view.force
  )

  def singleArtist(artist: Artist, mf: MusicFinder): ExistingAlbumsCache = {
    val artistDir = mf.genreDirs
        .flatMap(_.deepDirs)
        .find(_.name.toLowerCase == artist.name.toLowerCase)
        .get
    ExistingAlbumsCache.from(artistDir.dirs
        .mapIf(_.isEmpty).to(Vector(artistDir))
        .flatMap(NewAlbumsRetriever.dirToAlbum(_, mf)))
  }
}
