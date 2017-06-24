package search

import common.Jsonable
import common.io.{DirectoryRef, JsonableSaver}
import models.{Album, Artist, Song}
import search.ModelsJsonable._

/** Index for songs, albums and artists */
class CompositeIndex(implicit r: DirectoryRef) {
  import Index.ProductOrdering
  import WeightedIndexable.ops._
  private val indexBuilder = WeightedIndexBuilder
  private val saver = new JsonableSaver()
  private def buildIndexFromCache[T: Jsonable : WeightedIndexable : Manifest] =
    indexBuilder.buildIndexFor(saver.loadArray[T]) // don't know why [T] is needed here
  private def find(terms: Seq[String]) = new { // currying because Scala isn't functional enough :(
    def apply[T: Jsonable : WeightedIndexable : Manifest](index: Index[T]): Seq[T] =
      index findIntersection terms take 10 sortBy (_.sortBy)
  }
  private val songIndex = buildIndexFromCache[Song]
  private val albumIndex = buildIndexFromCache[Album]
  private val artistIndex = buildIndexFromCache[Artist]
  def search(terms: Seq[String]): (Seq[Song], Seq[Album], Seq[Artist]) = {
    val finder = find(terms)
    (finder(songIndex), finder(albumIndex), finder(artistIndex))
  }
}
