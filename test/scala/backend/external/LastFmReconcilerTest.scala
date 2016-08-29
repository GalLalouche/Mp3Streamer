package backend.external

import java.io.FileInputStream
import java.net.HttpURLConnection

import backend.Url
import backend.configs.TestConfiguration
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
    $(Artist("Foobar")).get shouldReturn None
  }
  "200" in {
    implicit val c = this.c.copy(_httpTransformer = new FakeHttpURLConnection(_) {
      override def getResponseCode: Int = HttpURLConnection.HTTP_OK
      override def getContent: AnyRef = new FileInputStream(getResourceFile("last_fm.html"))
    })
    new LastFmReconciler().apply(Artist("dreamtheater")).get.get shouldReturn
        ExternalLink[Artist](Url("http://www.last.fm/music/Dream+Theater"), Host.LastFm)
  }
}
