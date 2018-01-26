package backend.external.expansions
import java.net.HttpURLConnection

import backend.Url
import backend.configs.{FakeWSResponse, TestConfiguration}
import backend.external.{BaseLink, DocumentSpecs, Host}
import common.io.WSAliases._
import common.rich.RichFuture._
import common.rich.RichT._
import common.rich.primitives.RichBoolean._
import org.scalatest.FreeSpec

class AllMusicHelperTest extends FreeSpec with DocumentSpecs {
  private implicit val config = TestConfiguration()
  private def withDocument(s: String) = config.copy(_urlToBytesMapper = getBytes(s).partialConst)
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
    val url = Url("http://foobar")
    "hasRating" - {
      "yes" in {
        helperPointsToValid.hasRating(url).get shouldReturn true
      }
      "no" in {
        helperPointsToEmpty.hasRating(url).get shouldReturn false
      }
    }
    "hasStaffReview" - {
      "yes" in {
        helperPointsToValid.hasStaffReview(url).get shouldReturn true
      }
      "no" in {
        helperPointsToEmpty.hasStaffReview(url).get shouldReturn false
      }
    }
    "isValid" - {
      "yes" in {
        helperPointsToValid.isValidLink(url).get shouldReturn true
      }
      "no" in {
        helperPointsToEmpty.isValidLink(url).get shouldReturn false
      }
    }
  }

  "canonize" - {
    "mw link" in {
      $.canonize(
        BaseLink(Url("http://www.allmusic.com/album/born-in-the-usa-mw0000191830"), Host.Wikipedia))
          .get.link.address shouldReturn "http://www.allmusic.com/album/born-in-the-usa-mw0000191830"
    }
    "rlink" - {
      def withRedirectingMock(sourceToDest: (String, String)*) = {
        val asMap = sourceToDest.toMap
        implicit val config = this.config.copy(_requestToResponseMapper = {
          case r: WSRequest if asMap.contains(r.url) && r.followRedirects.exists(_.isFalse) =>
            FakeWSResponse(
              status = HttpURLConnection.HTTP_MOVED_PERM,
              allHeaders = Map("location" -> Seq(asMap(r.url))))
        })
        new AllMusicHelper
      }
      "regular" in {
        withRedirectingMock("http://www.allmusic.com/album/r827504" ->
            "http://www.allmusic.com/album/home-mw0000533017")
            .canonize(BaseLink(Url("http://www.allmusic.com/album/r827504"), Host.Wikipedia))
            .get.link.address shouldReturn "http://www.allmusic.com/album/home-mw0000533017"
      }
      "without www" in {
        withRedirectingMock("http://www.allmusic.com/album/ghost-r2202519" ->
            "http://www.allmusic.com/album/ghost-mw0002150605")
            .canonize(BaseLink(Url("http://www.allmusic.com/album/ghost-r2202519"), Host.Wikipedia))
            .get.link.address shouldReturn "http://www.allmusic.com/album/ghost-mw0002150605"
      }
      "multiple retries" - {
        "succeeds after second" in {
          withRedirectingMock(
            "http://www.allmusic.com/album/ghost-r2202519" -> "http://www.allmusic.com/album/ghost-r2202520",
            "http://www.allmusic.com/album/ghost-r2202520" -> "http://www.allmusic.com/album/ghost-mw0002150605")
              .canonize(BaseLink(Url("http://www.allmusic.com/album/ghost-r2202519"), Host.Wikipedia))
              .get.link.address shouldReturn "http://www.allmusic.com/album/ghost-mw0002150605"
        }
        "gives up eventually, returning the last url" in {
          withRedirectingMock(
            "http://www.allmusic.com/album/ghost-r2202519" -> "http://www.allmusic.com/album/ghost-r2202520",
            "http://www.allmusic.com/album/ghost-r2202520" -> "http://www.allmusic.com/album/ghost-r2202520")
              .canonize(BaseLink(Url("http://www.allmusic.com/album/ghost-r2202519"), Host.Wikipedia))
              .get.link.address shouldReturn "http://www.allmusic.com/album/ghost-r2202520"
        }
      }
    }
  }
}
