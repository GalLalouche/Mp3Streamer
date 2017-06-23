package search

import common.rich.RichT._

/** extracts several terms from each song to match against */
private object TermIndexBuilder extends IndexBuilder with ToIndexableOps {
  implicit class RichMap[T, S](map: Map[T, Set[S]]) {
    def append(t: T, s: S) = map.updated(t, map(t) + s)
  }
  // TODO replace with some kind of map builder from RichTraversableOnce
  def buildIndexFor[T: Indexable](ts: TraversableOnce[T]): Index[T] = ts
      ./:(Map[String, Set[T]]().withDefault(Set[T]().const)) {(map, indexable) =>
        indexable
            .terms
            .map(_.toLowerCase)
            .foldLeft(map)((map, word) => map.append(word, indexable))
      }.map(e => (e._1, e._2.toVector))
      .mapTo(new MapIndex(_))
}
