package backend.external

import backend.TestConfiguration
import backend.recon.Album
import common.AuxSpecs
import common.rich.collections.RichTraversableOnce._
import common.rich.path.RichFile._
import org.jsoup.Jsoup
import org.scalatest.FreeSpec

class WikipediaAlbumExternalLinksExpanderTest extends FreeSpec with AuxSpecs {
  private implicit val config = TestConfiguration

  private def getDocument(s: String) = Jsoup.parse(getResourceFile(s).readAll)

  def get(s: String): String =
    new WikipediaAlbumExternalLinksExpander().aux(getDocument(s))
        .filter(_.host.name == "allmusic")
        .map(_.link.address)
        .single

  "extract allmusic link" in {
    get("allmusic_link.html") shouldReturn "http://www.allmusic.com/album/born-in-the-usa-mw0000191830"
  }
  "extract allmusic2 link" in {
    get("allmusic_link_2.html") shouldReturn "http://www.allmusic.com/album/the-metal-opera-r540587"
  }
  "succeed even if there is no link" in {
    new WikipediaAlbumExternalLinksExpander().aux(getDocument("no_link.html")) shouldReturn Nil
  }
}
