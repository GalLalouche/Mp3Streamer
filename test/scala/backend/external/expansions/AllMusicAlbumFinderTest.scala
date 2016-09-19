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
        .get shouldReturn Url("https://www.allmusic.com/album/a-night-at-the-opera-mw0000391519")
  }
}
