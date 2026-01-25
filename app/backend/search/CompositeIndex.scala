package backend.search

import backend.search.CompositeIndex.Finder
import backend.search.ProductOrdering.Impl
import backend.search.WeightedIndexable.ops._
import models.{AlbumDir, ArtistDir, Song}

/** Index for songs, albums and artists. */
private class CompositeIndex(
    val songs: Index[Song],
    albums: Index[AlbumDir],
    artists: Index[ArtistDir],
) {
  def search(terms: Seq[String]): (Seq[Song], Seq[AlbumDir], Seq[ArtistDir]) = {
    val finder = new Finder(terms)
    (finder(songs), finder(albums), finder(artists))
  }
}

private object CompositeIndex {
  private class Finder(terms: Seq[String]) {
    def apply[T: WeightedIndexable: Manifest](index: Index[T]): Seq[T] =
      index.findIntersection(terms).take(10).sortBy(_.sortBy)
  }
}
