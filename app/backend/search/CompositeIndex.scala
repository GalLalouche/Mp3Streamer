package backend.search

import common.Jsonable
import common.io.{DirectoryRef, FormatSaver}
import models.{Album, Artist, Song}
import models.ModelJsonable._
import play.api.libs.json.Format

/** Index for songs, albums and artists. */
// TODO HLists ;)
class CompositeIndex private(songs: Index[Song], albums: Index[Album], artists: Index[Artist]) {
  import Index.ProductOrdering
  import WeightedIndexable.ops._
  private def find(terms: Seq[String]) = new { // currying because Scala isn't functional enough :(
    def apply[T: Format : WeightedIndexable : Manifest](index: Index[T]): Seq[T] =
      index findIntersection terms take 10 sortBy (_.sortBy)
  }
  def search(terms: Seq[String]): (Seq[Song], Seq[Album], Seq[Artist]) = {
    val finder = find(terms)
    (finder(songs), finder(albums), finder(artists))
  }
}

object CompositeIndex {
  def create(implicit r: DirectoryRef): CompositeIndex = {
    val saver = new FormatSaver
    val indexBuilder = WeightedIndexBuilder
    def buildIndexFromCache[T: Format : WeightedIndexable : Manifest] =
      indexBuilder.buildIndexFor(saver.loadArray[T])
    new CompositeIndex(buildIndexFromCache[Song], buildIndexFromCache[Album], buildIndexFromCache[Artist])
  }
}
