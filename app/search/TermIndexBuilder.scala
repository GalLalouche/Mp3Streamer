package search

import models.Song
import common.rich.RichT._

/** extracts several terms from each song to match against */
object TermIndexBuilder extends IndexBuilder {
  override def buildIndexFor(songs: TraversableOnce[Song]): Index = songs
    .foldLeft(Map[String, Set[Song]]().withDefault(Set[Song]()))(
      (map, song) => song.title.toLowerCase.split(" ").foldLeft(map)(
        (map, word) => map.updated(word, map(word) + song)))
    .map(e => e._1 -> e._2.toVector)
    .mapTo(new Index(_))
}