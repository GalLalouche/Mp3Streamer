package backend.external

import common.AuxSpecs
import common.rich.collections.RichTraversableOnce._
import common.rich.path.RichFile._
import org.jsoup.Jsoup
import org.scalatest.FreeSpec

class WikipediaAlbumExternalLinksExpanderTest extends FreeSpec with AuxSpecs {
  def getDocument(s: String) = Jsoup.parse(getResourceFile(s).readAll)
  "extract allmusic link" in {
    val allMusicLink =
      WikipediaAlbumExternalLinksExpander(getDocument("allmusic_link.html"))
          .filter(_.host.name == "allmusic")
          .map(_.link.address)
          .single
    allMusicLink shouldReturn "http://www.allmusic.com/album/born-in-the-usa-mw0000191830"
  }
  "succeed even if there is no link" in {
    WikipediaAlbumExternalLinksExpander(getDocument("no_link.html")) shouldReturn Nil
  }
}
