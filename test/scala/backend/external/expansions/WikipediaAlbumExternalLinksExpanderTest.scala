package backend.external.expansions

import java.net.HttpURLConnection

import backend.Url
import backend.configs.TestConfiguration
import backend.external.{ExternalLink, FakeHttpURLConnection, Host}
import common.AuxSpecs
import common.rich.RichFuture._
import common.rich.collections.RichTraversableOnce._
import common.rich.path.RichFile._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.FreeSpec

class WikipediaAlbumExternalLinksExpanderTest extends FreeSpec with AuxSpecs {
  private implicit val config = TestConfiguration().copy(_documentDownloader = u => getDocument(u.address))

  private def getDocument(s: String): Document = Jsoup.parse(getResourceFile(s).readAll)

  private val $: WikipediaAlbumExternalLinksExpander = new WikipediaAlbumExternalLinksExpander()
  private def getAllMusicLinkAddress(s: String): String =
    $.parseDocument(getDocument(s))
        .filter(_.host.name == "allmusic")
        .map(_.link.address)
        .single

  "extract allmusic link" in {
    getAllMusicLinkAddress("allmusic_link.html") shouldReturn "http://www.allmusic.com/album/born-in-the-usa-mw0000191830"
  }
  "canonize allmusic links" - {
    "mw link" in {
      $(ExternalLink(Url("allmusic_link.html"), Host.Wikipedia))
          .get.single.link.address shouldReturn "http://www.allmusic.com/album/born-in-the-usa-mw0000191830"
    }
    "rlink" - {
      def withRedirection(source: String, destination: String) = {
        def redirect(source: String, destination: String)(http: HttpURLConnection) = new FakeHttpURLConnection(http) {
          assert(http.getURL.toString.equals(source) && !http.getInstanceFollowRedirects)
          override def getResponseCode: Int = HttpURLConnection.HTTP_MOVED_PERM
          override def getHeaderField(s: String): String =
            if (s == "location") destination else throw new AssertionError()
        }
        implicit val config = this.config.copy(_httpTransformer = redirect(source, destination))
        new WikipediaAlbumExternalLinksExpander()
      }
      "regular" in {
        withRedirection("http://www.allmusic.com/album/r827504", "http://www.allmusic.com/album/home-mw0000533017")
            .apply(ExternalLink(Url("allmusic_rlink.html"), Host.Wikipedia))
            .get.single.link.address shouldReturn "http://www.allmusic.com/album/home-mw0000533017"
      }
      "without www" in {
        withRedirection("http://www.allmusic.com/album/ghost-r2202519", "http://www.allmusic.com/album/ghost-mw0002150605")
            .apply(ExternalLink(Url("allmusic_rlink2.html"), Host.Wikipedia))
            .get.single.link.address shouldReturn "http://www.allmusic.com/album/ghost-mw0002150605"
      }
    }
  }
  "Return nothing if response code isn't 301 or 200" in {
    implicit val config = this.config.copy(_httpTransformer = new FakeHttpURLConnection(_) {
      override def getResponseCode: Int = HttpURLConnection.HTTP_INTERNAL_ERROR
      override def getHeaderField(s: String): String = throw new AssertionError() // makes sure it isn't called
    })
    new WikipediaAlbumExternalLinksExpander().apply(ExternalLink(Url("allmusic_rlink.html"), Host.Wikipedia))
        .get shouldReturn Nil
  }
  "succeed even if there is no link" in {
    new WikipediaAlbumExternalLinksExpander().parseDocument(getDocument("no_link.html")) shouldReturn Nil
  }
}
