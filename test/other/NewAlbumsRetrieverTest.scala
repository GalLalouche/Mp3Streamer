package other

import org.junit.runner.RunWith
import org.scalatest.FreeSpec
import org.scalatest.mock.MockitoSugar
import org.scalatest.matchers.ShouldMatchers
import models.MusicFinder
import org.scalatest.junit.JUnitRunner
import org.scalatest.OneInstancePerTest
import org.mockito.Mockito._
import models.Song
import models.Album
import org.mockito.Matchers._

@RunWith(classOf[JUnitRunner])
class NewAlbumsRetrieverTest extends FreeSpec with MockitoSugar with ShouldMatchers with OneInstancePerTest {
	val metadata = mock[MetadataRetriever]
	val finder = mock[MusicFinder]
	val $ = new NewAlbumsRetriever {
		val meta = metadata
		val music = finder
	}

	"Find new albums by artist should" - {
		"throw an exception if artist isn't found" in {
			when(finder.getAlbums).thenReturn(Iterator(Album("song", 2000, "album")))
			when(metadata.getAlbums(anyString())).thenThrow(new NoSuchElementException)
			evaluating { $.findNewAlbums.toSeq } should produce[NoSuchElementException]
		}
		"return an empty sequence if there are no artists" in {
			when(finder.getAlbums).thenReturn(Iterator())
			$.findNewAlbums.toSeq should be === Seq()
		}
		", when there is actual data to retrieve, " - {
			val song1 = mock[Song];
			when(song1.artist).thenReturn("foo")
			val song2 = mock[Song];
			when(song2.artist).thenReturn("blur") // teehee
			val albums = Set(Album("foo", 2000, "bar"), Album("Blur", 1997, "Blur"), Album("Blur", 1999, "13"))
			when(metadata.getAlbums("foo")).thenReturn(albums.filter(_.artist == "foo").toIterator)
			when(metadata.getAlbums("blur")).thenReturn(Seq(Album("Blur", 1997, "Blur"), Album("Blur", 1999, "13")).toIterator)
			"return all albums for the artists" in {
				when(finder.getAlbums).thenReturn(albums
						.groupBy(_.artist)
						.map(_._2.head)
						.map(e => Album(e.artist, 0, e.albumName))
						.iterator)
				$.findNewAlbums.toSet should be === albums
			}
			"filter albums that are older than the latest album" in {
				when(finder.getAlbums).thenReturn(albums
						.groupBy(_.artist)
						.map(_._2.head)
						.map(e => Album(e.artist, 1997, e.albumName))
						.iterator)
				$.findNewAlbums.toSet should be === albums.filterNot(_.albumName == "Blur")

			}
		}
	}
}