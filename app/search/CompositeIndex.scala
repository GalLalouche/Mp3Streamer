package search

import models.{Album, Artist, MusicFinder, Song}

class CompositeIndex(mf: MusicFinder, indexBuilder: IndexBuilder) {
  private val saver = new JsonableSaver(mf.dir)
  private def buildIndexFromCache[T: Jsonable : Indexable](implicit m: Manifest[T]) =
    indexBuilder.buildIndexFor(saver.load)
  private def find(terms: Seq[String]) = new {
    // currying because Scala isn't functional enough :(
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
