package server

import backend.module.FakeWSResponse
import backend.recon.{AlbumReconStorage, ArtistReconStorage, ReconIDArbitrary}
import com.google.inject.Module
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import play.api.libs.json.Json
import sttp.client3.UriContext

import scala.concurrent.Future

import cats.implicits.catsSyntaxFlatMapOps

import common.rich.func.kats.ToMoreFunctorOps.toMoreFunctorOps

import common.test.BeforeAndAfterEachAsync

private class ExternalTest(module: Module)
    extends HttpServerSpecs(module)
    with BeforeAndAfterEachAsync {
  protected override def baseTestModule = super.baseTestModule.copy(
    _urlToResponseMapper = { case _ => FakeWSResponse(status = 404) },
  )

  // Must use a relative path because Http4sUtils.decodePath strips the leading '/'.
  private val songPath = relativePath(getResourceFile("/models/song.mp3"))

  private val artistReconStorage = injector.instance[ArtistReconStorage]
  private val albumReconStorage = injector.instance[AlbumReconStorage]

  override def beforeEach(): Future[_] =
    artistReconStorage.utils.clearOrCreateTable() >>
      albumReconStorage.utils.clearOrCreateTable()

  // All external APIs are mocked to 404; these just verify the endpoints respond with valid JSON.
  "get external links" in { getJson(uri"external/$songPath") >| succeed }
  "refresh artist" in { getJson(uri"external/refresh/artist/$songPath") >| succeed }
  "refresh album" in { getJson(uri"external/refresh/album/$songPath") >| succeed }
  "update artist recon" in {
    val reconId = ReconIDArbitrary.gen.sample.get.id
    postString(uri"external/recons/$songPath", Json.obj("artist" -> reconId)) >| succeed
  }
}
