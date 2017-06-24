package search

import common.ds.Collectable.setCollectable
import common.ds.RichMap.RichMapCollectable
import common.rich.RichT._

/** Extracts several terms from each song to match against. */
private object TermIndexBuilder extends IndexBuilder with Indexable.ToIndexableOps {
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
