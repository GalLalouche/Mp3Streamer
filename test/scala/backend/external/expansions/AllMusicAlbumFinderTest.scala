package backend.external.expansions

import backend.Url
import backend.configs.TestConfiguration
import backend.external.DocumentSpecs
import backend.recon.{Album, Artist}
import org.scalatest.FreeSpec

class AllMusicAlbumFinderTest extends FreeSpec with DocumentSpecs {
  private implicit val c = TestConfiguration()
  private val $ = new AllMusicAlbumFinder
  "Find links" in {
    $.findAlbum(getDocument("allmusic_discography.html"), Album("A night at the opera", 1975, Artist("Queen")))
        .get shouldReturn Url("www.allmusic.com/album/a-night-at-the-opera-mw0000391519")
  }
  "Handle case of missing input in discography list" in {
    $.findAlbum(getDocument("allmusic_discography2.html"), Album("The Ghost of Tom Joad", 1995, Artist("Bruce Springsteen")))
        .get shouldReturn Url("www.allmusic.com/album/the-ghost-of-tom-joad-mw0000181768")
  }
}
