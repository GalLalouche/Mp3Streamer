package search

import models.Song

/** builds the index for exact matches */
object SimpleIndexBuilder extends IndexBuilder {
  private def groupBy[T, S](ts: TraversableOnce[T], f: T => S): Map[S, Seq[T]] = {
    var map = Map[S, List[T]]()
    for (t <- ts) {
      val key = f(t)
      map += ((key, t :: map.get(key).getOrElse(Nil)))
    }
    map
  }
  override def buildIndexFor(songs: TraversableOnce[Song]) = new Index(
    groupBy[Song, String](songs, _.title.toLowerCase)
      .map(e => e._1 -> e._2.toVector)
      .toMap)
}