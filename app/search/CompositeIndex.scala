package search

import models.Song
import models.Artist
import models.Album

class CompositeIndex private (indexBuilder: IndexBuilder) {
  private def buildIndexFromCache[T: Jsonable: Indexable](implicit m: Manifest[T]) =
    indexBuilder.buildIndexFor(MetadataCacher.load[T])
  private val songIndex = buildIndexFromCache[Song]
  private val albumIndex = buildIndexFromCache[Album]
  private val artistIndex = buildIndexFromCache[Artist]
  def search(terms: Seq[String]) =
    (songIndex.findIntersection(terms), albumIndex.findIntersection(terms), artistIndex.findIntersection(terms))
}

object CompositeIndex {
  def buildWith(indexBuilder: IndexBuilder) = new CompositeIndex(indexBuilder)
}