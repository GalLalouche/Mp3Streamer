package backend.external.expansions

import java.net.HttpURLConnection

import backend.Url
import backend.configs.{Configuration, FakeWSResponse, TestConfiguration}
import backend.external.{BaseLink, DocumentSpecs, Host}
import common.rich.RichFuture._
import common.rich.RichT._
import common.rich.collections.RichTraversableOnce._
import org.scalatest.FreeSpec

class WikipediaAlbumExternalLinksExpanderTest extends FreeSpec with DocumentSpecs {
  private implicit val config: TestConfiguration =
    TestConfiguration().copy(_urlToBytesMapper = PartialFunction(getBytes))

  private val $: WikipediaAlbumExternalLinksExpander = new WikipediaAlbumExternalLinksExpander()
  private def getAllMusicLinkAddress(s: String): String =
    $.parseDocument(getDocument(s))
        .filter(_.host == Host.AllMusic)
        .map(_.link.address)
        .single

  "extract allmusic link" in {
    getAllMusicLinkAddress("allmusic_link.html") shouldReturn
        "http://www.allmusic.com/album/born-in-the-usa-mw0000191830"
  }
  "Return nothing on error" in {
    implicit val config: Configuration = this.config.copy(_urlToResponseMapper =
        FakeWSResponse(status = HttpURLConnection.HTTP_INTERNAL_ERROR).partialConst)
    new WikipediaAlbumExternalLinksExpander()
        .apply(BaseLink(Url("allmusic_rlink.html"), Host.Wikipedia))
        .get shouldReturn Nil
  }
  "succeed even if there is no link" in {
    new WikipediaAlbumExternalLinksExpander().parseDocument(getDocument("no_link.html")) shouldReturn Nil
  }
}
