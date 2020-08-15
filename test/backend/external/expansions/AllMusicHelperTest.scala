package backend.external.expansions

import java.net.HttpURLConnection

import backend.Url
import backend.external.{BaseLink, DocumentSpecs, Host}
import backend.module.{FakeWSResponse, TestModuleConfiguration}
import backend.recon.Album
import net.codingwell.scalaguice.InjectorExtensions._
import org.scalatest.AsyncFreeSpec

import common.io.WSAliases._
import common.rich.RichT._
import common.rich.primitives.RichBoolean._
import common.test.AsyncAuxSpecs

class AllMusicHelperTest extends AsyncFreeSpec with AsyncAuxSpecs with DocumentSpecs {
  private val config = TestModuleConfiguration()
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
    "hasRating" - {
      "yes" in {
        AllMusicHelper.hasRating(getDocument("allmusic_has_rating.html")) shouldReturn true
      }
      "no" in {
        AllMusicHelper.hasRating(getDocument("allmusic_no_rating.html")) shouldReturn false
      }
    }
    "hasStaffReview" - {
      "yes" in {
        AllMusicHelper.hasStaffReview(getDocument("allmusic_has_rating.html")) shouldReturn true
      }
      "no" in {
        AllMusicHelper.hasStaffReview(getDocument("allmusic_no_rating.html")) shouldReturn false
      }
    }
    "isValid" - {
      val helperPointsToValid = create(withDocument("allmusic_has_rating.html"))
      val helperPointsToEmpty = create(withDocument("allmusic_no_rating.html"))
      val url = Url("http://foobar")
      "yes" in {
        helperPointsToValid.isValidLink(url) shouldEventuallyReturn true
      }
      "no" in {
        helperPointsToEmpty.isValidLink(url) shouldEventuallyReturn false
      }
    }
  }

  "canonize" - {
    "mw link" in {
      $.canonize(
        BaseLink[Album](Url("http://www.allmusic.com/album/born-in-the-usa-mw0000191830"), Host.Wikipedia))
          .map(_.link.address shouldReturn "http://www.allmusic.com/album/born-in-the-usa-mw0000191830")
    }
    "rlink" - {
      def withRedirectingMock(sourceToDest: (String, String)*) = {
        val asMap = sourceToDest.toMap
        create(this.config.copy(_requestToResponseMapper = {
          case r: WSRequest if asMap.contains(r.url) && r.followRedirects.exists(_.isFalse) =>
            FakeWSResponse(
              status = HttpURLConnection.HTTP_MOVED_PERM,
              allHeaders = Map("location" -> Vector(asMap(r.url))))
        }))
      }
      "regular" in {
        withRedirectingMock("http://www.allmusic.com/album/r827504" ->
            "http://www.allmusic.com/album/home-mw0000533017")
            .canonize(BaseLink[Album](Url("http://www.allmusic.com/album/r827504"), Host.Wikipedia))
            .map(_.link.address shouldReturn "http://www.allmusic.com/album/home-mw0000533017")
      }
      val link = BaseLink[Album](Url("http://www.allmusic.com/album/ghost-r2202519"), Host.Wikipedia)
      "without www" in {
        withRedirectingMock("http://www.allmusic.com/album/ghost-r2202519" ->
            "http://www.allmusic.com/album/ghost-mw0002150605")
            .canonize(link)
            .map(_.link.address shouldReturn "http://www.allmusic.com/album/ghost-mw0002150605")
      }
      "multiple retries" - {
        "succeeds after second" in {
          withRedirectingMock(
            "http://www.allmusic.com/album/ghost-r2202519" -> "http://www.allmusic.com/album/ghost-r2202520",
            "http://www.allmusic.com/album/ghost-r2202520" -> "http://www.allmusic.com/album/ghost-mw0002150605")
              .canonize(link)
              .map(_.link.address shouldReturn "http://www.allmusic.com/album/ghost-mw0002150605")
        }
        "gives up eventually, returning the last url" in {
          withRedirectingMock(
            "http://www.allmusic.com/album/ghost-r2202519" -> "http://www.allmusic.com/album/ghost-r2202520",
            "http://www.allmusic.com/album/ghost-r2202520" -> "http://www.allmusic.com/album/ghost-r2202520")
              .canonize(link)
              .map(_.link.address shouldReturn "http://www.allmusic.com/album/ghost-r2202520")
        }
      }
    }
  }
}
