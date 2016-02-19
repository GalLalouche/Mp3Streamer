package search

import models.Song
import scala.annotation.tailrec

class Index(map: Map[String, Seq[Song]]) {
  private def sort(songs: Seq[Song]): Seq[Song] = {
    @tailrec
    def sort(s1: Song, s2: Song, fs: List[Song => String]): Boolean = {
      val comparison = fs.head(s1).compareTo(fs.head(s2))
      if (comparison != 0) comparison < 0 else sort(s1, s2, fs.tail)
    }
    songs.sortWith(sort(_, _, List(_.artist, _.year.toString, _.track.toString)))
  }
  def find(s: String) = sort(map.get(s.toLowerCase).getOrElse(Nil))
}