package search

import models.Song
import common.rich.RichT._

/** extracts several terms from each song to match against */
object TermIndexBuilder extends IndexBuilder {
  def buildIndexFor[T: Indexable](songs: TraversableOnce[T]): Index[T] = {
    songs
      .foldLeft(Map[String, Set[T]]().withDefault(Set[T]()))(
        (map, indexable) => implicitly[Indexable[T]].terms(indexable).map(_.toLowerCase).foldLeft(map)(
          (map, word) => map.updated(word, map(word) + indexable)))
      .map(e => e._1 -> e._2.toVector)
      .mapTo(new Index(_))
  }
}