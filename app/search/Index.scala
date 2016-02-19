package search

import models.Song

class Index(map: Map[String, Seq[Song]]) {
  def find(s: String) = map.get(s).getOrElse(Nil)
}