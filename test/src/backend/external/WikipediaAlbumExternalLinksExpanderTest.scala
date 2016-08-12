package backend.external

import backend.TestConfiguration
import common.AuxSpecs
import common.rich.collections.RichTraversableOnce._
import common.rich.path.RichFile._
import org.jsoup.Jsoup
import org.scalatest.FreeSpec

class WikipediaAlbumExternalLinksExpanderTest extends FreeSpec with AuxSpecs {
  private implicit val config = TestConfiguration

  def getDocument(s: String) = Jsoup.parse(getResourceFile(s).readAll)

  "extract allmusic link" in {
    val allMusicLink =
      new WikipediaAlbumExternalLinksExpander().aux(getDocument("allmusic_link.html"))
        .filter(_.host.name == "allmusic")
        .map(_.link.address)
        .single
    allMusicLink shouldReturn "http://www.allmusic.com/album/born-in-the-usa-mw0000191830"
  }
  "succeed even if there is no link" in {
    new WikipediaAlbumExternalLinksExpander().aux(getDocument("no_link.html")) shouldReturn Nil
  }
}
