package backend.external.recons

import java.net.HttpURLConnection

import backend.Url
import backend.configs.{FakeWSResponse, TestConfiguration}
import backend.external.{BaseLink, DocumentSpecs, Host}
import backend.recon.Artist
import common.AuxSpecs
import common.io.InternetTalker
import common.rich.RichFuture._
import common.rich.RichT._
import net.codingwell.scalaguice.InjectorExtensions._
import org.scalatest.FreeSpec

import scala.concurrent.ExecutionContext

class LastFmLinkRetrieverTest extends FreeSpec with AuxSpecs with DocumentSpecs {
  private val config = new TestConfiguration
  private implicit val ec: ExecutionContext = config.injector.instance[ExecutionContext]
  private def create(config: TestConfiguration): LastFmLinkRetriever = {
    new LastFmLinkRetriever(config.injector.instance[InternetTalker], millisBetweenRedirects = 1)
  }
  "404" in {
    val c = config.copy(_urlToResponseMapper =
        FakeWSResponse(status = HttpURLConnection.HTTP_NOT_FOUND).partialConst)
    create(c)(Artist("Foobar")).get shouldBe 'empty
  }
  "200" in {
    val c = config.copy(_urlToBytesMapper = getBytes("last_fm.html").partialConst)
    create(c)(Artist("dreamtheater")).get.get shouldReturn
        BaseLink[Artist](Url("http://www.last.fm/music/Dream+Theater"), Host.LastFm)
  }
  "302" in {
    var first = false
    val c = config.copy(_urlToResponseMapper = {
      case _ if first =>
        first = false
        FakeWSResponse(status = HttpURLConnection.HTTP_MOVED_TEMP)
      case _ =>
        FakeWSResponse(status = HttpURLConnection.HTTP_OK, bytes = getBytes("last_fm.html"))
    })
    create(c)(Artist("Foobar")).get.get shouldReturn
        BaseLink[Artist](Url("http://www.last.fm/music/Dream+Theater"), Host.LastFm)
  }
}
