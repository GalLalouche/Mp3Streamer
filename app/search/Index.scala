package search

import models.Song
import scala.annotation.tailrec

class Index private (map: Map[String, Seq[Song]]) {
  private val sortingPriority = List[Function[Song, String]](_.artist, _.year.toString, _.album, _.track.toString)
  private def sort(songs: Seq[Song]): Seq[Song] =
    songs.sortWith((s1, s2) => sortingPriority.map(f => f(s1).compareTo(f(s2))).find(_ != 0).getOrElse(1) > 0)
  def find(s: String) = map.get(s.toLowerCase).getOrElse(Nil)
  def findIntersection(ss: TraversableOnce[String]): Seq[Song] = {
    def findSet(s: String) = find(s).toSet
    val list = ss.toList
    val intersection = list.tail.foldLeft(findSet(list.head))((agg, term) => agg.intersect(findSet(term)))
    sort(intersection.toSeq)
  }
}

object Index {
  def apply(map: Map[String, Seq[Song]]): Index = new Index(map)
}