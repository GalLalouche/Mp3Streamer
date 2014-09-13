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

@RunWith(classOf[JUnitRunner])
class NewAlbumsRetrieverTest extends FreeSpec with MockitoSugar with ShouldMatchers with OneInstancePerTest {
	val md = mock[MetadataRetriever]
	val mf = mock[MusicFinder]
	val $ = new NewAlbumsRetriever {
		val meta = md
		val music = mf
	}

	"Find new albums by artist should" - {
		"throw an exception if artist isn't found" in {
			val song = mock[Song];
			when(song.artist).thenReturn("foobar")
			when(mf.getSongs).thenReturn(Seq(song))
			when(md.getAlbums("foobar")).thenThrow(new NoSuchElementException)
			evaluating { $.findNewAlbums.toSeq } should produce[NoSuchElementException]
		}
		"return an empty sequence if there are no artists" in {
			when(mf.getSongs).thenReturn(Seq())
			$.findNewAlbums.toSeq should be === Seq()
		}
		", when there is actual data to retrieve, " - {
			val song1 = mock[Song];
			when(song1.artist).thenReturn("foo")
			val song2 = mock[Song];
			when(song2.artist).thenReturn("blur") // teehee
			when(mf.getSongs).thenReturn(Seq(song1, song2))
			val albums = Set(Album("foo", 2002, "bar"), Album("Blur", 1997, "Blur"), Album("Blur", 1999, "13"))
			when(md.getAlbums("foo")).thenReturn(albums.filter(_.artist == "foo").toSeq)
			when(md.getAlbums("blur")).thenReturn(albums.filter(_.artist == "Blur").toSeq)
			when(md.getAlbums("blur")).thenReturn(Seq(Album("Blur", 1997, "Blur"), Album("Blur", 1999, "13")))
			"return all albums for the artists" in {
				$.findNewAlbums.toSet should be === albums
			}
			"filter albums that are older than the latest album" in {
				when(song2.year).thenReturn(1997)
				$.findNewAlbums.toSet should be === albums.filterNot(_.albumName == "Blur")

			}
		}
	}
}