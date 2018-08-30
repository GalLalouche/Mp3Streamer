package backend.search

import common.json.Jsonable
import models.{Album, Artist, Song}
import models.ModelJsonable._

/** Index for songs, albums and artists. */
// TODO HLists ;)
private class CompositeIndex(songs: Index[Song], albums: Index[Album], artists: Index[Artist]) {
  import Index.ProductOrdering
  import WeightedIndexable.ops._

  private def find(terms: Seq[String]) = new { // currying because Scala isn't functional enough :(
    def apply[T: Jsonable : WeightedIndexable : Manifest](index: Index[T]): Seq[T] =
      index findIntersection terms take 10 sortBy (_.sortBy)
  }
  def search(terms: Seq[String]): (Seq[Song], Seq[Album], Seq[Artist]) = {
    val finder = find(terms)
    (finder(songs), finder(albums), finder(artists))
  }
}
