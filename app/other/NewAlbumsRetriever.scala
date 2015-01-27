package other

import models.MusicFinder
import models.Album
import common.rich.RichAll._

class NewAlbumsRetriever(meta: MetadataRetriever, music: MusicFinder, ignoredArtists: Seq[String]) {
	var lastArtist: Option[String] = None
	def findNewAlbums: Iterator[Album] = {
		val lastAlbums = music.getAlbums
			.toSeq
			.groupBy(_.artist)
			.map(e => e._1.toLowerCase -> e._2.toVector
				.sortBy(_.year)
				.map(_.year)
				.last // finds the last album
			).toMap
		lastAlbums.keys.iterator
			.filterNot(ignoredArtists.contains)
			.flatMap(meta.getAlbums)
			.filter(e =>
				if (lastAlbums(e.artist.toLowerCase) < e.year)
					true
				else {
					for (a <- lastArtist)
						if (a != e.artist) println("Finished " + a)
					lastArtist = Some(e.artist)
					false
				})
	}
}