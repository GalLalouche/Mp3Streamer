package backend.external

import java.io.File

import common.AuxSpecs
import common.rich.path.RichFile._
import org.jsoup.Jsoup
import org.scalatest.FreeSpec
import common.rich.collections.RichTraversableOnce._

class WikipediaAlbumExternalLinksExpenderTest extends FreeSpec with AuxSpecs {
  "foobar" in {
    val d = Jsoup.parse(getResourceFile("example.html").readAll)
    val allMusicLink =
      WikipediaAlbumExternalLinksExpender(d).filter(_.host.name == "allmusic").map(_.link.address).single
    allMusicLink shouldReturn "http://www.allmusic.com/album/born-in-the-usa-mw0000191830"
  }
}
