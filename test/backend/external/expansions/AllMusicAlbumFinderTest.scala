package backend.external.expansions

import backend.Url
import backend.configs.TestConfiguration
import backend.external.{BaseLink, DocumentSpecs, Host}
import backend.recon.{Album, Artist}
import common.rich.RichFuture._
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.FreeSpec
import org.scalatest.mockito.MockitoSugar

import scala.concurrent.Future

class AllMusicAlbumFinderTest extends FreeSpec with DocumentSpecs with MockitoSugar {
  "findAlbum" - {
    implicit val c = TestConfiguration()
    val $ = new AllMusicAlbumFinder()
    "Find links" in {
      $.findAlbum(getDocument("allmusic_discography.html"), Album("A night at the opera", 1975, Artist("Queen")))
          .get shouldReturn Url("http://www.allmusic.com/album/a-night-at-the-opera-mw0000391519")
    }
    "Missing input in discography list" in {
      $.findAlbum(getDocument("allmusic_discography2.html"), Album("The Ghost of Tom Joad", 1995, Artist("Bruce Springsteen")))
          .get shouldReturn Url("http://www.allmusic.com/album/the-ghost-of-tom-joad-mw0000181768")
    }
    "href already has host name" in {
      $.findAlbum(getDocument("allmusic_discography3.html"), Album("A Wintersunset", 1996, Artist("Empyrium")))
          .get shouldReturn Url("http://www.allmusic.com/album/a-wintersunset-mw0001654263")
    }
  }

  "apply" - {
    val artistLink = BaseLink[Artist](Url("url"), Host.AllMusic)
    // TODO extract to a document configurations
    implicit val c = TestConfiguration(_urlToBytesMapper = {
      case Url("url/discography") => getBytes("allmusic_discography.html")
    })
    val allMusicHelper = mock[AllMusicHelper]
    val $ = new AllMusicAlbumFinder(allMusicHelper)
    "invalid link returns empty" in {
      when(allMusicHelper.isValidLink(any())).thenReturn(Future successful false)
      $.apply(artistLink, Album("A night at the opera", 1975, Artist("Queen"))).get shouldReturn None
    }
    "valid links returns the link" in {
      when(allMusicHelper.isValidLink(any())).thenReturn(Future successful true)
      $.apply(artistLink, Album("A night at the opera", 1975, Artist("Queen"))).get.get shouldReturn
          BaseLink[Album](Url("http://www.allmusic.com/album/a-night-at-the-opera-mw0000391519"), Host.AllMusic)
    }
  }
}
