package server

import backend.module.FakeWSResponse
import backend.recon.{AlbumReconStorage, ArtistReconStorage, ReconIDArbitrary}
import com.google.inject.Module
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import play.api.libs.json.{JsObject, Json}
import sttp.client3.UriContext

import scala.concurrent.Future

import cats.implicits.catsSyntaxFlatMapOps

import common.test.BeforeAndAfterEachAsync

private class ExternalTest(module: Module)
    extends HttpServerSpecs(module)
    with BeforeAndAfterEachAsync {
  protected override def baseTestModule = super.baseTestModule.copy(
    _urlToResponseMapper = { case _ => FakeWSResponse(status = 404) },
  )

  // Must use a relative path because Http4sUtils.decodePath strips the leading '/'.
  private val file = getResourceFile("/models/song.mp3")
  private val songPath = new java.io.File(".").getCanonicalFile.toPath.relativize(file.toPath).toString

  private val artistReconStorage = injector.instance[ArtistReconStorage]
  private val albumReconStorage = injector.instance[AlbumReconStorage]

  override def beforeEach(): Future[_] =
    artistReconStorage.utils.clearOrCreateTable() >>
      albumReconStorage.utils.clearOrCreateTable()

  "get external links" in {
    getJson(uri"external/$songPath").map { json =>
      val obj = json.as[JsObject]
      (obj \ "Artist" \ "error").asOpt[String] should not be empty
      (obj \ "Album" \ "error").asOpt[String] should not be empty
    }
  }

  "refresh artist" in {
    getJson(uri"external/refresh/artist/$songPath").map { json =>
      val obj = json.as[JsObject]
      (obj \ "Artist" \ "error").asOpt[String] should not be empty
      (obj \ "Album" \ "error").asOpt[String] should not be empty
    }
  }

  "refresh album" in {
    getJson(uri"external/refresh/album/$songPath").map { json =>
      val obj = json.as[JsObject]
      (obj \ "Artist" \ "error").asOpt[String] should not be empty
      (obj \ "Album" \ "error").asOpt[String] should not be empty
    }
  }

  "update artist recon" in {
    val reconId = ReconIDArbitrary.gen.sample.get.id
    val body = Json.obj("artist" -> reconId)
    postString(uri"external/recons/$songPath", body).map { response =>
      // The recon update returns refreshed external links; with mocked 404s,
      // the response should be a JSON object (possibly with error fields).
      val json = Json.parse(response)
      json shouldBe a[JsObject]
    }
  }
}
