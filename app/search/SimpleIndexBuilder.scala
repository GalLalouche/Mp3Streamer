package search

import models.Song

/** builds the index for exact matches */
object SimpleIndexBuilder extends IndexBuilder {
  private def groupBy[T, S](ts: TraversableOnce[T], f: T => S): Map[S, Seq[T]] = {
    ts.foldLeft(Map[S, List[T]]().withDefault(e => Nil)) {
      case (map, t) =>
        val key = f(t)
        map.updated(key, t :: map(key))
    }
  }
  override def buildIndexFor(songs: TraversableOnce[Song]) = Index(
    groupBy[Song, String](songs, _.title.toLowerCase)
      .map(e => e._1 -> e._2.toVector)
      .toMap)
}