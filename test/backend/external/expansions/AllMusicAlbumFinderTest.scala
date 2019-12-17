package backend.external.expansions

import backend.Url
import backend.recon.{Album, Artist}
import net.codingwell.scalaguice.ScalaModule
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import org.scalatest.OptionValues._

import scala.concurrent.Future

class AllMusicAlbumFinderTest extends SameHostExpanderSpec with MockitoSugar {
  private val allMusicHelper = mock[AllMusicHelper]
  override def module = new ScalaModule {
    override def configure() = {
      bind[SameHostExpander].to[AllMusicAlbumFinder]
      bind[AllMusicHelper] toInstance allMusicHelper
    }
  }
  override protected val expandingUrl = artistUrl + "/discography"

  "apply" - {
    when(allMusicHelper.isValidLink(any())).thenReturn(Future successful true)
    "Find links" in {
      findAlbum("allmusic_discography.html", Album("A night at the opera", 1975, Artist("Queen")))
          .map(_.value.link shouldReturn Url("http://www.allmusic.com/album/a-night-at-the-opera-mw0000391519"))
    }
    "Missing input in discography list" in {
      findAlbum("allmusic_discography2.html", Album("The Ghost of Tom Joad", 1995, Artist("Bruce Springsteen")))
          .map(_.value.link shouldReturn Url("http://www.allmusic.com/album/the-ghost-of-tom-joad-mw0000181768"))
    }
    "href already has host name" in {
      findAlbum("allmusic_discography3.html", Album("A Wintersunset", 1996, Artist("Empyrium")))
          .map(_.value.link shouldReturn Url("http://www.allmusic.com/album/a-wintersunset-mw0001654263"))
    }
    "additional hrefs in <td>s chooses the one with .title prefix" in {
      findAlbum("allmusic_discography4.html", Album("Clouds", 1992, Artist("Tiamat")))
          .map(_.value.link shouldReturn Url("https://www.allmusic.com/album/clouds-mw0000103448"))
    }
    "invalid link returns None" in {
      when(allMusicHelper.isValidLink(any())).thenReturn(Future successful false)
      findAlbum("allmusic_discography.html", Album("A night at the opera", 1975, Artist("Queen")))
          .map(_ shouldReturn None)
    }
  }
}
