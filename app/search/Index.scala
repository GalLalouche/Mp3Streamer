package search

import models.Song
import scala.annotation.tailrec

class Index[T: Indexable](map: Map[String, Seq[T]]) {
  private val sortingPriority = List[Function[Song, String]](_.artistName, _.year.toString, _.albumName, _.track.toString)
  private def sort(songs: Seq[Song]): Seq[Song] =
    songs.sortWith((s1, s2) => sortingPriority.map(f => f(s1).compareTo(f(s2))).find(_ != 0).getOrElse(1) > 0)
  def find(s: String) = map.get(s.toLowerCase).getOrElse(Nil)
  def findIntersection(ss: Traversable[String]): Seq[T] = {
    if (ss.size == 1) // optimization for a single term; no need to insert into a set
      return find(ss.head)
    def findAsSet(s: String) = find(s).toSet
    val list = ss.toList
    val intersection = list.tail.foldLeft(findAsSet(list.head))((agg, term) => agg.intersect(findAsSet(term)))
    intersection.toSeq.sortWith(implicitly[Indexable[T]].compare)
  }
}
