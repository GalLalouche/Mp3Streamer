package backend.lyrics.retrievers

import backend.StorageSetup
import backend.module.TestModuleConfiguration
import backend.lyrics.Instrumental
import common.AuxSpecs
import common.rich.RichFuture._
import models.FakeModelFactory
import net.codingwell.scalaguice.InjectorExtensions._
import org.scalatest.FreeSpec

import scala.concurrent.ExecutionContext

class InstrumentalArtistTest extends FreeSpec with AuxSpecs with StorageSetup {
  override protected val config = TestModuleConfiguration()
  private implicit val ec: ExecutionContext = config.injector.instance[ExecutionContext]
  override protected lazy val storage = config.injector.instance[InstrumentalArtistStorage]
  private val factory = new FakeModelFactory
  private val $ = config.injector.instance[InstrumentalArtist]

  "exists" in {
    val song = factory.song(artistName = "foo")
    storage.store("foo").get
    $(song).get shouldReturn Instrumental("Default for Artist")
  }
  "doesn't exist" in {
    val song = factory.song(artistName = "foo")
    storage.store("bar").get
    $(song).getFailure
  }
}
