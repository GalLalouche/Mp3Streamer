package backend.external.expansions

import backend.external.{BaseLink, DocumentSpecs, Host}
import backend.module.{FakeWSResponse, TestModuleConfiguration}
import backend.recon.Album
import common.io.WSAliases._
import common.rich.RichT._
import common.rich.primitives.RichBoolean._
import common.test.AsyncAuxSpecs
import io.lemonlabs.uri.Url
import java.net.HttpURLConnection
import net.codingwell.scalaguice.InjectorExtensions._
import org.scalatest.AsyncFreeSpec

class AllMusicHelperTest extends AsyncFreeSpec with AsyncAuxSpecs with DocumentSpecs {
  private val config = TestModuleConfiguration()
  private def withDocument(s: String) = config.copy(_urlToBytesMapper = getBytes(s).partialConst)
  private def create(c: TestModuleConfiguration) = c.injector.instance[AllMusicHelper]
  private val $ = create(config)

  "isCanonical" - {
    "yes" in {
      $.isCanonical("http://www.allmusic.com/album/machine-head-mw0000189625") shouldReturn true
    }
    "no" - {
      "rlink" in {
        $.isCanonical("http://www.allmusic.com/album/r5332/review") shouldReturn false
      }
      "partial mw-link" in {
        $.isCanonical("http://www.allmusic.com/album/mw0000189625") shouldReturn false
      }
    }
  }

  private val validLink = "allmusic_helper_has_rating_has_staff_review.html"
  private val ratingNoReview = "allmusic_helper_rating_but_no_staff_review.html"
  private val noRatingNoReview = "allmusic_helper_no_rating_no_staff_review.html"
  private val reviewNoRating = "allmusic_helper_staff_review_but_no_rating.html"
  "validity" - {
    "hasRating" - {
      "yes" in {
        AllMusicHelper.hasRating(getDocument(validLink)) shouldReturn true
        AllMusicHelper.hasRating(getDocument(ratingNoReview)) shouldReturn true
      }
      "no" in {
        AllMusicHelper.hasRating(getDocument(noRatingNoReview)) shouldReturn false
        AllMusicHelper.hasRating(getDocument(reviewNoRating)) shouldReturn false
      }
    }
    "hasStaffReview" - {
      "yes" in {
        AllMusicHelper.hasStaffReview(getDocument(validLink)) shouldReturn true
        AllMusicHelper.hasStaffReview(getDocument(reviewNoRating)) shouldReturn true
      }
      "no" in {
        AllMusicHelper.hasStaffReview(getDocument(noRatingNoReview)) shouldReturn false
        AllMusicHelper.hasStaffReview(getDocument(ratingNoReview)) shouldReturn false
      }
    }
    "isValid" - {
      val url = Url.parse("http://foobar")
      "yes" in {
        create(withDocument(validLink))
          .isValidLink(url) shouldEventuallyReturn true
      }
      "no" - {
        Vector(
          reviewNoRating,
          ratingNoReview,
          noRatingNoReview,
        ).foreach(s =>
          s in {
            create(withDocument(s)).isValidLink(url) shouldEventuallyReturn false
          },
        )
      }
    }
  }

  "canonize" - {
    "mw link" in {
      $.canonize(
        BaseLink[Album](
          Url.parse("http://www.allmusic.com/album/born-in-the-usa-mw0000191830"),
          Host.Wikipedia,
        ),
      )
        .map(
          _.link.toStringPunycode shouldReturn "http://www.allmusic.com/album/born-in-the-usa-mw0000191830",
        )
    }
    "rlink" - {
      def withRedirectingMock(sourceToDest: (String, String)*) = {
        val asMap = sourceToDest.toMap
        create(this.config.copy(_requestToResponseMapper = {
          case r: WSRequest if asMap.contains(r.url) && r.followRedirects.exists(_.isFalse) =>
            FakeWSResponse(
              status = HttpURLConnection.HTTP_MOVED_PERM,
              allHeaders = Map("location" -> Vector(asMap(r.url))),
            )
        }))
      }
      "regular" in {
        withRedirectingMock(
          "http://www.allmusic.com/album/r827504" ->
            "http://www.allmusic.com/album/home-mw0000533017",
        )
          .canonize(
            BaseLink[Album](Url.parse("http://www.allmusic.com/album/r827504"), Host.Wikipedia),
          )
          .map(
            _.link.toStringPunycode shouldReturn "http://www.allmusic.com/album/home-mw0000533017",
          )
      }
      val link =
        BaseLink[Album](Url.parse("http://www.allmusic.com/album/ghost-r2202519"), Host.Wikipedia)
      "without www" in {
        withRedirectingMock(
          "http://www.allmusic.com/album/ghost-r2202519" ->
            "http://www.allmusic.com/album/ghost-mw0002150605",
        )
          .canonize(link)
          .map(
            _.link.toStringPunycode shouldReturn "http://www.allmusic.com/album/ghost-mw0002150605",
          )
      }
      "multiple retries" - {
        "succeeds after second" in {
          withRedirectingMock(
            "http://www.allmusic.com/album/ghost-r2202519" -> "http://www.allmusic.com/album/ghost-r2202520",
            "http://www.allmusic.com/album/ghost-r2202520" -> "http://www.allmusic.com/album/ghost-mw0002150605",
          )
            .canonize(link)
            .map(
              _.link.toStringPunycode shouldReturn "http://www.allmusic.com/album/ghost-mw0002150605",
            )
        }
        "gives up eventually, returning the last url" in {
          withRedirectingMock(
            "http://www.allmusic.com/album/ghost-r2202519" -> "http://www.allmusic.com/album/ghost-r2202520",
            "http://www.allmusic.com/album/ghost-r2202520" -> "http://www.allmusic.com/album/ghost-r2202520",
          )
            .canonize(link)
            .map(
              _.link.toStringPunycode shouldReturn "http://www.allmusic.com/album/ghost-r2202520",
            )
        }
      }
    }
  }
}
