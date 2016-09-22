package backend.external.expansions

import java.net.HttpURLConnection

import backend.Url
import backend.configs.TestConfiguration
import backend.external.{ExternalLink, FakeHttpURLConnection, Host}
import common.AuxSpecs
import org.scalatest.FreeSpec
import common.rich.RichFuture._

class AllMusicHelperTest extends FreeSpec with AuxSpecs {
  private implicit val config = TestConfiguration()
  private val $ = new AllMusicHelper
  "isCanonical" - {
    "yes" in {
      $ isCanonical "http://www.allmusic.com/album/machine-head-mw0000189625" shouldReturn true
    }
    "no" - {
      "rlink" in {
        $ isCanonical "http://www.allmusic.com/album/r5332/review" shouldReturn false
      }
      "partial mw-link" in {
        $ isCanonical "http://www.allmusic.com/album/mw0000189625" shouldReturn false
      }
    }
  }
  "canonize" - {
    "mw link" in {
      $.canonize(ExternalLink(Url("http://www.allmusic.com/album/born-in-the-usa-mw0000191830"), Host.Wikipedia))
          .get.link.address shouldReturn "http://www.allmusic.com/album/born-in-the-usa-mw0000191830"
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
        new AllMusicHelper
      }
      "regular" in {
        withRedirection("http://www.allmusic.com/album/r827504", "http://www.allmusic.com/album/home-mw0000533017")
            .canonize(ExternalLink(Url("http://www.allmusic.com/album/r827504"), Host.Wikipedia))
            .get.link.address shouldReturn "http://www.allmusic.com/album/home-mw0000533017"
      }
      "without www" in {
        withRedirection("http://www.allmusic.com/album/ghost-r2202519", "http://www.allmusic.com/album/ghost-mw0002150605")
            .canonize(ExternalLink(Url("http://www.allmusic.com/album/ghost-r2202519"), Host.Wikipedia))
            .get.link.address shouldReturn "http://www.allmusic.com/album/ghost-mw0002150605"
      }
    }
  }
}
