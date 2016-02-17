package models

import java.io.File

class SimpleMusicSearcher(songs: => Traversable[Song]) extends MusicSearcher {
	lazy val index = songs
		.groupBy(_.artist.toLowerCase)
		.map(e => e._1 -> e._2.toVector)
		.toMap
	override def apply(s: String) = index.getOrElse(s, Nil)
}