package backend.external

import java.net.HttpURLConnection

import backend.{TestConfiguration, Url}
import common.AuxSpecs
import common.rich.RichFuture._
import common.rich.collections.RichTraversableOnce._
import common.rich.path.RichFile._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.FreeSpec

class WikipediaAlbumExternalLinksExpanderTest extends FreeSpec with AuxSpecs {
  private implicit val config = TestConfiguration.withDocumentDownloader(u => getDocument(u.address))
  private class FakeHttpURLConnection(httpURLConnection: HttpURLConnection) extends HttpURLConnection(httpURLConnection.getURL) {
    override def disconnect(): Unit = throw new AssertionError()
    override def usingProxy(): Boolean = throw new AssertionError()
    override def connect(): Unit = throw new AssertionError()
  }

  private def getDocument(s: String): Document = Jsoup.parse(getResourceFile(s).readAll)

  private def get(s: String): String =
    new WikipediaAlbumExternalLinksExpander().aux(getDocument(s))
        .filter(_.host.name == "allmusic")
        .map(_.link.address)
        .single

  "extract allmusic link" in {
    get("allmusic_link.html") shouldReturn "http://www.allmusic.com/album/born-in-the-usa-mw0000191830"
  }
  "canonize allmusic links" in {
    implicit val config = this.config.withHttpConnector(http => new FakeHttpURLConnection(http) {
      assert(http.getURL.toString.equals("http://www.allmusic.com/album/r827504") && !http.getInstanceFollowRedirects)
      override def getResponseCode: Int = HttpURLConnection.HTTP_MOVED_PERM
      override def getHeaderField(s: String): String =
        if (s == "location") "http://www.allmusic.com/album/home-mw0000533017" else throw new AssertionError()
    })
    new WikipediaAlbumExternalLinksExpander().apply(ExternalLink(Url("allmusic_rlink.html"), Host.Wikipedia))
        .get.single.link.address shouldReturn "http://www.allmusic.com/album/home-mw0000533017"
  }
  "Return nothing if response code isn't 301" in {
    implicit val config = this.config.withHttpConnector(new FakeHttpURLConnection(_) {
      override def getResponseCode: Int = HttpURLConnection.HTTP_INTERNAL_ERROR
      override def getHeaderField(s: String): String = throw new AssertionError() // makes sure it isn't called
    })
    new WikipediaAlbumExternalLinksExpander().apply(ExternalLink(Url("allmusic_rlink.html"), Host.Wikipedia))
        .get shouldReturn Nil
  }
  "succeed even if there is no link" in {
    new WikipediaAlbumExternalLinksExpander().aux(getDocument("no_link.html")) shouldReturn Nil
  }
}
