package backend.lyrics.retrievers

import backend.StorageSetup
import backend.configs.TestModuleConfiguration
import common.AuxSpecs
import common.rich.RichFuture._
import net.codingwell.scalaguice.InjectorExtensions._
import org.scalatest.FreeSpec

import scala.concurrent.ExecutionContext

class InstrumentalArtistStorageTest extends FreeSpec with AuxSpecs with StorageSetup {
  override protected val config = TestModuleConfiguration()
  private implicit val ec: ExecutionContext = config.injector.instance[ExecutionContext]
  override protected def storage = config.injector.instance[InstrumentalArtistStorage]
  private val artistName = "foo"

  "store and load" in {
    storage.load(artistName).get shouldReturn None
    storage.store(artistName).get
    storage.load(artistName).get shouldReturn Some(())
  }
  "delete" in {
    storage.store(artistName).get
    storage.delete(artistName).get
    storage.load(artistName).get shouldReturn None
  }
}
