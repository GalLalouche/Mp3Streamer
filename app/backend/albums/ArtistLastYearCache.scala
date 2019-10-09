package backend.albums

import backend.mb.MbArtistReconciler.MbAlbumMetadata
import backend.recon.{Album, Artist}

import common.rich.RichT._

private class ArtistLastYearCache private(lastReleaseYear: Map[Artist, Int]) {
  import ArtistLastYearCache._

  def artists: Iterable[Artist] = lastReleaseYear.keys

  def filterNewAlbums(artist: Artist, albums: Seq[MbAlbumMetadata]): Seq[NewAlbumRecon] = for {
    album <- albums
    if album.isOut && lastReleaseYear(canonicalize(artist)) < album.releaseDate.getYear
  } yield NewAlbumRecon(NewAlbum.from(artist, album), album.reconId)
}

private object ArtistLastYearCache {
  private def canonicalize(a: Artist): Artist = Artist(a.normalize)
  def from(albums: Seq[Album]) = new ArtistLastYearCache(albums
      .groupBy(_.artist |> canonicalize)
      .mapValues(_.toVector.map(_.year).max)
      .view.force
  )
}
