package other

import models.MusicFinder
import models.Album

class NewAlbumsRetriever(meta: MetadataRetriever, music: MusicFinder, ignoredArtists: Seq[String]) {

	def findNewAlbums: Iterator[Album] = {
		val lastAlbums = music.getAlbums
			.toSeq
			.groupBy(_.artist)
			.map(e => e._1.toLowerCase -> e._2.map(_.year).last) // take last album
			.toMap
		lastAlbums.keys.iterator
			.filterNot(ignoredArtists.contains)
			.flatMap(meta.getAlbums)
			.filter(e => lastAlbums(e.artist.toLowerCase) < e.year)
	}
}