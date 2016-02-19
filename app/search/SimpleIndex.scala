package search

import models.Song

class SimpleIndex(songs: => Seq[Song]) extends Index {
	lazy val index = songs
		.groupBy(_.title.toLowerCase)
		.map(e => e._1 -> e._2.toVector)
		.toMap
	override protected def get(s: String) = index get s
}