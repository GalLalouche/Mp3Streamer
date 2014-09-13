package other

import models.MusicFinder
import models.Album

abstract class NewAlbumsRetriever {
	val meta: MetadataRetriever
	val music: MusicFinder

	def findNewAlbums: Iterator[Album] = {
		val lastAlbums = music.getAlbums
			.toSeq
			.groupBy(_.artist)
			.map(e => e._1.toLowerCase -> e._2.map(_.year).last) // take last album
			.toMap
		println(lastAlbums)
		lastAlbums.keys.iterator
			.flatMap(meta.getAlbums)
			.filter(e => lastAlbums(e.artist.toLowerCase) < e.year)
	}
}