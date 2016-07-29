package backend.external

import common.AuxSpecs
import common.rich.collections.RichTraversableOnce._
import common.rich.path.RichFile._
import org.jsoup.Jsoup
import org.scalatest.FreeSpec

class WikipediaAlbumExternalLinksExpanderTest extends FreeSpec with AuxSpecs {
  "foobar" in {
    val d = Jsoup.parse(getResourceFile("example.html").readAll)
    val allMusicLink =
      WikipediaAlbumExternalLinksExpander(d).filter(_.host.name == "allmusic").map(_.link.address).single
    allMusicLink shouldReturn "http://www.allmusic.com/album/born-in-the-usa-mw0000191830"
  }
}
