package search

import common.rich.RichT._

/** extracts several terms from each song to match against */
private object TermIndexBuilder extends IndexBuilder {
  implicit class RichMap[T, S](map: Map[T, Set[S]]) {
    def append(t: T, s: S) = map.updated(t, map(t) + s)
  }
  def buildIndexFor[T: Indexable](songs: TraversableOnce[T]): Index[T] = songs
    .foldLeft(Map[String, Set[T]]().withDefault(Set[T]().const))((map, indexable) => implicitly[Indexable[T]]
      .terms(indexable)
      .map(_.toLowerCase)
      .foldLeft(map)((map, word) => map.append(word, indexable)))
    .map(e => (e._1, e._2.toVector))
    .mapTo(new MapIndex(_))
}
