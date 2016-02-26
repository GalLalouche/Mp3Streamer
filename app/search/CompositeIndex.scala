package search

import models.Song
import models.Artist
import models.Album

class CompositeIndex private (indexBuilder: IndexBuilder) {
  private def buildIndexFromCache[T: Jsonable: Indexable](implicit m: Manifest[T]) =
    indexBuilder.buildIndexFor(MetadataCacher.load[T])
  def find[T](terms: Seq[String], index: Index[T]): Seq[T] =
    index findIntersection terms take 10
  private val songIndex = buildIndexFromCache[Song]
  private val albumIndex = buildIndexFromCache[Album]
  private val artistIndex = buildIndexFromCache[Artist]
  def search(terms: Seq[String]) =
    (find(terms, songIndex), find(terms, albumIndex), find(terms, artistIndex))
}

object CompositeIndex {
  def buildWith(indexBuilder: IndexBuilder) = new CompositeIndex(indexBuilder)
}