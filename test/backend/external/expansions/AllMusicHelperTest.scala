package backend.external.expansions

import java.net.HttpURLConnection

import backend.Url
import backend.configs.{FakeWSResponse, TestModuleConfiguration}
import backend.external.{BaseLink, DocumentSpecs, Host}
import common.io.WSAliases._
import common.rich.RichFuture._
import common.rich.RichT._
import common.rich.primitives.RichBoolean._
import net.codingwell.scalaguice.InjectorExtensions._
import org.scalatest.FreeSpec

import scala.concurrent.ExecutionContext

class AllMusicHelperTest extends FreeSpec with DocumentSpecs {
  private val config = TestModuleConfiguration()
  private implicit val ec: ExecutionContext = config.injector.instance[ExecutionContext]
  private def withDocument(s: String) = config.copy(_urlToBytesMapper = getBytes(s).partialConst)
  private def create(c: TestModuleConfiguration) = c.injector.instance[AllMusicHelper]
  private val $ = create(config)
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
    val helperPointsToValid = create(withDocument("allmusic_has_rating.html"))
    val helperPointsToEmpty = create(withDocument("allmusic_no_rating.html"))
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
        create(this.config.copy(_requestToResponseMapper = {
          case r: WSRequest if asMap.contains(r.url) && r.followRedirects.exists(_.isFalse) =>
            FakeWSResponse(
              status = HttpURLConnection.HTTP_MOVED_PERM,
              allHeaders = Map("location" -> Seq(asMap(r.url))))
        }))
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
