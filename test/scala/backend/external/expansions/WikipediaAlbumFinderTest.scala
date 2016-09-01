package backend.external.expansions

import backend.Url
import backend.configs.TestConfiguration
import backend.external.DocumentSpecs
import backend.recon.{Album, Artist}
import org.scalatest.FreeSpec

class WikipediaAlbumFinderTest extends FreeSpec with DocumentSpecs {
  private implicit val c = new TestConfiguration
  "extract the album when possible" in {
    val $ = new WikipediaAlbumFinder()
    $.aux(getDocument("wikipedia-discography.html"), Album("Lady in Gold", 2016, Artist("Blues Pills")))
        .get shouldReturn Url("https://en.wikipedia.org/wiki/Lady_in_Gold_(album)")
  }
}