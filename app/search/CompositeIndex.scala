package search

import common.io.DirectoryRef
import models.{Album, Artist, Song}

/** Index for songs, albums and artists */
class CompositeIndex(implicit r: DirectoryRef) {
  import Index.ProductOrdering
  val indexBuilder = WeightedIndexBuilder
  private val saver = new JsonableSaver()
  private def buildIndexFromCache[T: Jsonable : Indexable : WeightedIndexable : Manifest] =
    indexBuilder.buildIndexFor(saver.load)
  private def find(terms: Seq[String]) = new { // currying because Scala isn't functional enough :(
    def apply[T: Jsonable : Indexable : WeightedIndexable : Manifest](index: Index[T]): Seq[T] =
      index findIntersection terms take 10 sortBy implicitly[Indexable[T]].sortBy
  }
  private val songIndex = buildIndexFromCache[Song]
  private val albumIndex = buildIndexFromCache[Album]
  private val artistIndex = buildIndexFromCache[Artist]
  def search(terms: Seq[String]) = {
    val finder = find(terms)
    (finder(songIndex), finder(albumIndex), finder(artistIndex))
  }
}
