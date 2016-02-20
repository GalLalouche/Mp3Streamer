package search

import models.Song
import scala.annotation.tailrec

class Index private (map: Map[String, Seq[Song]]) {
  // this should happen at construction

  def find(s: String) = map.get(s.toLowerCase).getOrElse(Nil)
}

object Index {
  private def sort(songs: Seq[Song]): Seq[Song] = {
    @tailrec
    def aux(s1: Song, s2: Song, fs: List[Song => String]): Boolean = {
      if (fs.isEmpty) return true
      val comparison = fs.head(s1).compareTo(fs.head(s2))
      if (comparison != 0) comparison < 0 else aux(s1, s2, fs.tail)
    }
    songs.sortWith(aux(_, _, List(_.artist, _.year.toString, _.album, _.track.toString)))
  }
  def apply(map: Map[String, Seq[Song]]): Index = {
    new Index(map.map(e => e._1 -> sort(e._2)))
  }
}