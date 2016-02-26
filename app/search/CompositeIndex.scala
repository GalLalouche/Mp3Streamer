package search

import models.{ Album, Artist, Song }

class CompositeIndex private (indexBuilder: IndexBuilder) {
  private def buildIndexFromCache[T: Jsonable: Indexable](implicit m: Manifest[T]) =
    indexBuilder.buildIndexFor(MetadataCacher.load[T])
  private def find(terms: Seq[String]) = new { // currying because Scala isn't functional enough :(
    def apply[T](index: Index[T]): Seq[T] = index findIntersection terms take 10
  }
  private val songIndex = buildIndexFromCache[Song]
  private val albumIndex = buildIndexFromCache[Album]
  private val artistIndex = buildIndexFromCache[Artist]
  def search(terms: Seq[String]) = {
    val finder = find(terms)
    (finder(songIndex), finder(albumIndex), finder(artistIndex))
  }
}

object CompositeIndex {
  def buildWith(indexBuilder: IndexBuilder) = new CompositeIndex(indexBuilder)
}