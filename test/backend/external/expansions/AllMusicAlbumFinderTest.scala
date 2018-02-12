package backend.external.expansions

import backend.Url
import backend.configs.TestConfiguration
import backend.recon.{Album, Artist}
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar

import scala.concurrent.Future

class AllMusicAlbumFinderTest extends SameHostExpanderSpec with MockitoSugar {
  private val allMusicHelper = mock[AllMusicHelper]
  override private[expansions] def createExpander(implicit c: TestConfiguration) =
    new AllMusicAlbumFinder(allMusicHelper)
  override protected val expandingUrl = artistUrl + "/discography"

  "apply" - {
    when(allMusicHelper.isValidLink(any())).thenReturn(Future successful true)
    "Find links" in {
      findAlbum("allmusic_discography.html", Album("A night at the opera", 1975, Artist("Queen")))
          .get.link shouldReturn Url("http://www.allmusic.com/album/a-night-at-the-opera-mw0000391519")
    }
    "Missing input in discography list" in {
      findAlbum("allmusic_discography2.html", Album("The Ghost of Tom Joad", 1995, Artist("Bruce Springsteen")))
          .get.link shouldReturn Url("http://www.allmusic.com/album/the-ghost-of-tom-joad-mw0000181768")
    }
    "href already has host name" in {
      findAlbum("allmusic_discography3.html", Album("A Wintersunset", 1996, Artist("Empyrium")))
          .get.link shouldReturn Url("http://www.allmusic.com/album/a-wintersunset-mw0001654263")
    }
    "invalid link returns None" in {
      when(allMusicHelper.isValidLink(any())).thenReturn(Future successful false)
      findAlbum("allmusic_discography.html", Album("A night at the opera", 1975, Artist("Queen"))
      ) shouldReturn None
    }
  }
}
