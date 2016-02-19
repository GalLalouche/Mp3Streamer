package mains.albums

import models.{ Album, MusicFinder }

private class NewAlbumsRetriever(meta: MetadataRetriever, music: MusicFinder, ignoredArtists: Seq[String]) {
	var lastArtist: Option[String] = None
	def getLastAlbum(e: (String, Seq[Album])) = e._1.toLowerCase -> e._2.toVector
		.sortBy(_.year)
		.map(_.year)
		.last
	def findNewAlbums: Iterator[Album] = {
		val lastAlbumsByArtist = music.getAlbums
			.toSeq
			.groupBy(_.artist.toLowerCase)
			.map(getLastAlbum)
			.toMap
		def isNewAlbum(e: Album): Boolean = // assumes they are sorted by year... perhaps it shouldn't :|
			if (lastAlbumsByArtist(e.artist.toLowerCase) < e.year)
				true
			else {
				for (a <- lastArtist)
					if (a != e.artist) println("Finished " + a)
				lastArtist = Some(e.artist)
				false
			}
		lastAlbumsByArtist.keys.iterator
			.filterNot(ignoredArtists.contains)
			.flatMap(meta.getAlbums(_)) // has default argument, can't bind by name
			.filter(isNewAlbum)
	}
}