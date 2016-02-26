package search

import models.Song
import models.Artist
import models.Album

class CompositeIndex(indexBuilder: IndexBuilder) {
  private def buildIndexFromCache[T: Jsonable: Indexable](implicit m: Manifest[T]) =
    indexBuilder.buildIndexFor(MetadataCacher.load[T])
  private var songIndex: Index[Song] = null
  private var albumIndex: Index[Album] = null
  private var artistIndex: Index[Artist] = null
  def buildIndex() {
    songIndex = buildIndexFromCache
    albumIndex = buildIndexFromCache
    artistIndex = buildIndexFromCache
  }
  def search(terms: Seq[String]) = 
    (songIndex.findIntersection(terms), albumIndex.findIntersection(terms), artistIndex.findIntersection(terms))
}