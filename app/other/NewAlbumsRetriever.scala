package other

import models.MusicFinder
import models.Album

abstract class NewAlbumsRetriever {
	val meta: MetadataRetriever
	val music: MusicFinder

	def findNewAlbums: Iterator[Album] = {
		val songs = music.getSongs
		val lastAlbums = songs
			.groupBy(_.artist)
			.map(e => e._1.toLowerCase -> e._2.map(_.year).last)
			.toMap
		songs
			.map(_.artist)
			.toSet
			.toIterator
			.flatMap(meta.getAlbums)
			.filter(e => lastAlbums(e.artist.toLowerCase) < e.year)
	}
}