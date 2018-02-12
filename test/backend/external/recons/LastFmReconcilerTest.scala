package backend.external.recons

import java.net.HttpURLConnection

import backend.Url
import backend.configs.{Configuration, FakeWSResponse, TestConfiguration}
import backend.external.{BaseLink, DocumentSpecs, Host}
import backend.recon.Artist
import common.AuxSpecs
import common.rich.RichFuture._
import common.rich.RichT._
import org.scalatest.FreeSpec

class LastFmReconcilerTest extends FreeSpec with AuxSpecs with DocumentSpecs {
  private val config = new TestConfiguration
  "404" in {
    implicit val c: Configuration = config.copy(_urlToResponseMapper =
        FakeWSResponse(status = HttpURLConnection.HTTP_NOT_FOUND).partialConst)
    val $ = new LastFmReconciler
    $(Artist("Foobar")).get shouldBe 'empty
  }
  "200" in {
    implicit val c: Configuration = config.copy(_urlToBytesMapper = getBytes("last_fm.html").partialConst)
    new LastFmReconciler().apply(Artist("dreamtheater")).get.get shouldReturn
        BaseLink[Artist](Url("http://www.last.fm/music/Dream+Theater"), Host.LastFm)
  }
  "302" in {
    var first = false
    implicit val c: Configuration = config.copy(_urlToResponseMapper = {
      case _ if first =>
        first = false
        FakeWSResponse(status = HttpURLConnection.HTTP_MOVED_TEMP)
      case _ =>
        FakeWSResponse(status = HttpURLConnection.HTTP_OK, bytes = getBytes("last_fm.html"))
    })
    new LastFmReconciler(1).apply(Artist("Foobar")).get.get shouldReturn
        BaseLink[Artist](Url("http://www.last.fm/music/Dream+Theater"), Host.LastFm)

  }
}
