package backend.lyrics.retrievers

import backend.StorageSetup
import backend.module.TestModuleConfiguration
import net.codingwell.scalaguice.InjectorExtensions._
import org.scalatest.AsyncFreeSpec

import scalaz.syntax.bind.ToBindOps
import common.rich.func.BetterFutureInstances._

class SlickInstrumentalArtistStorageTest extends AsyncFreeSpec with StorageSetup {
  override protected val config = TestModuleConfiguration()
  override protected def storage: InstrumentalArtistStorage =
    config.injector.instance[SlickInstrumentalArtistStorage]
  private val artistName = "foo"

  "store and load" in {
    storage.load(artistName).shouldEventuallyReturnNone() >>
        storage.store(artistName) >>
        storage.load(artistName).mapValue(_ shouldReturn())
  }
  "delete" in {
    storage.store(artistName) >> storage.delete(artistName).run >>
        storage.load(artistName).shouldEventuallyReturnNone()
  }
}
