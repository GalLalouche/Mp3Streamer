package backend.external.expansions

import java.net.HttpURLConnection

import backend.Url
import backend.configs.TestConfiguration
import backend.external.{DocumentSpecs, ExternalLink, FakeHttpURLConnection, Host}
import common.rich.RichFuture._
import common.rich.RichT._
import org.scalatest.FreeSpec

class AllMusicHelperTest extends FreeSpec with DocumentSpecs {
  private implicit val config = TestConfiguration()
  private def withDocument(s: String) = config.copy(_documentDownloader = getDocument(s).const)
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
  "validity" - {
    val helperPointsToValid = {
      implicit val config = withDocument("allmusic_has_rating.html")
      new AllMusicHelper()
    }
    val helperPointsToEmpty = {
      implicit val config = withDocument("allmusic_no_rating.html")
      new AllMusicHelper()
    }
    "hasRating" - {
      "yes" in {
        helperPointsToValid.hasRating(null).get shouldReturn true
      }
      "no" in {
        helperPointsToEmpty.hasRating(null).get shouldReturn false
      }
    }
    "hasStaffReview" - {
      "yes" in {
        helperPointsToValid.hasStaffReview(null).get shouldReturn true
      }
      "no" in {
        helperPointsToEmpty.hasStaffReview(null).get shouldReturn false
      }
    }
    "isValid" - {
      "yes" in {
        helperPointsToValid.isValidLink(null).get shouldReturn true
      }
      "no" in {
        helperPointsToEmpty.isValidLink(null).get shouldReturn false
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
