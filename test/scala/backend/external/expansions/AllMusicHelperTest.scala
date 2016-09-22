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
}
