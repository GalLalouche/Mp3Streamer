package backend.external.recons

import java.io.FileInputStream
import java.net.HttpURLConnection

import backend.Url
import backend.configs.TestConfiguration
import backend.external.{BaseLink, FakeHttpURLConnection, Host}
import backend.recon.Artist
import common.AuxSpecs
import common.rich.RichFuture._
import org.scalatest.FreeSpec

class LastFmReconcilerTest extends FreeSpec with AuxSpecs {
  private val c = new TestConfiguration
  "404" in {
    implicit val c = this.c.copy(_httpTransformer = new FakeHttpURLConnection(_) {
      override def getResponseCode: Int = HttpURLConnection.HTTP_NOT_FOUND
    })
    val $ = new LastFmReconciler
    $(Artist("Foobar")).get shouldBe 'empty
  }
  "200" in {
    implicit val c = this.c.copy(_httpTransformer = new FakeHttpURLConnection(_) {
      override def getResponseCode: Int = HttpURLConnection.HTTP_OK
      override def getContent: AnyRef = new FileInputStream(getResourceFile("last_fm.html"))
    })
    new LastFmReconciler().apply(Artist("dreamtheater")).get.get shouldReturn
        BaseLink[Artist](Url("http://www.last.fm/music/Dream+Theater"), Host.LastFm)
  }
  "302" in {
    implicit val c = this.c.copy(_httpTransformer = {
      var firstAttempt = true
      new FakeHttpURLConnection(_) {
        override def getResponseCode: Int = if (firstAttempt) {
          firstAttempt = false
          HttpURLConnection.HTTP_MOVED_TEMP
        } else
          HttpURLConnection.HTTP_OK
        override def getContent: AnyRef =
          if (!firstAttempt) new FileInputStream(getResourceFile("last_fm.html")) else null
      }
    })
    new LastFmReconciler(1).apply(Artist("Foobar")).get.get shouldReturn
        BaseLink[Artist](Url("http://www.last.fm/music/Dream+Theater"), Host.LastFm)

  }
}
