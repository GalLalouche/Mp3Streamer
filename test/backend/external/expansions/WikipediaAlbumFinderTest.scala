package backend.external.expansions

import backend.Url
import backend.configs.TestConfiguration
import backend.external.DocumentSpecs
import backend.recon.{Album, Artist}
import org.scalatest.FreeSpec

class WikipediaAlbumFinderTest extends FreeSpec with DocumentSpecs {
  private implicit val c = new TestConfiguration
  private val $ = new WikipediaAlbumFinder()
  "extract the album when possible" in {
    $.findAlbum(getDocument("wikipedia-discography.html"), Album("Lady in Gold", 2016, Artist("Blues Pills")))
        .get shouldReturn Url("https://en.wikipedia.org/wiki/Lady_in_Gold_(album)")
  }
  "Return nothing on red link" in {
    $.findAlbum(getDocument("wiki_redlink.html"), Album("Graveward", 2015, Artist("Sigh"))) shouldReturn None
  }
}
