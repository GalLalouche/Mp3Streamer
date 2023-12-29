package backend.external.recons

import java.net.HttpURLConnection

import backend.external.{BaseLink, DocumentSpecs, Host}
import backend.module.{FakeWSResponse, TestModuleConfiguration}
import backend.recon.Artist
import io.lemonlabs.uri.Url
import net.codingwell.scalaguice.InjectorExtensions._
import org.scalatest.AsyncFreeSpec

import common.io.InternetTalker
import common.rich.RichT._
import common.test.AsyncAuxSpecs

class LastFmLinkRetrieverTest extends AsyncFreeSpec with AsyncAuxSpecs with DocumentSpecs {
  private val config = new TestModuleConfiguration
  private def create(config: TestModuleConfiguration): LastFmLinkRetriever =
    new LastFmLinkRetriever(config.injector.instance[InternetTalker], millisBetweenRedirects = 1)
  "404" in {
    val c = config.copy(_urlToResponseMapper =
      FakeWSResponse(status = HttpURLConnection.HTTP_NOT_FOUND).partialConst,
    )
    create(c)(Artist("Foobar")).shouldEventuallyReturnNone()
  }
  "200" in {
    val c = config.copy(_urlToBytesMapper = getBytes("last_fm.html").partialConst)
    create(c)(Artist("dreamtheater"))
      .mapValue(
        _ shouldReturn BaseLink[Artist](
          Url.parse("http://www.last.fm/music/Dream+Theater"),
          Host.LastFm,
        ),
      )
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
    create(c)(Artist("Foobar"))
      .mapValue(
        _ shouldReturn BaseLink[Artist](
          Url.parse("http://www.last.fm/music/Dream+Theater"),
          Host.LastFm,
        ),
      )
  }
}
