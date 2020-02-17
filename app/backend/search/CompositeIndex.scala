package backend.search

import backend.search.CompositeIndex.Finder
import backend.search.ProductOrdering.Impl
import backend.search.WeightedIndexable.ops._
import models.{Album, Artist, Song}
import models.ModelJsonable._

import common.json.Jsonable

/** Index for songs, albums and artists. */
private class CompositeIndex(songs: Index[Song], albums: Index[Album], artists: Index[Artist]) {
  def search(terms: Seq[String]): (Seq[Song], Seq[Album], Seq[Artist]) = {
    val finder = new Finder(terms)
    (finder(songs), finder(albums), finder(artists))
  }
}

private object CompositeIndex {
  private class Finder(terms: Seq[String]) {
    def apply[T: Jsonable : WeightedIndexable : Manifest](index: Index[T]): Seq[T] =
      index findIntersection terms take 10 sortBy (_.sortBy)
  }
}
