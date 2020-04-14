package backend.lyrics.retrievers

import backend.StorageSetup
import backend.module.TestModuleConfiguration
import net.codingwell.scalaguice.InjectorExtensions._
import org.scalatest.AsyncFreeSpec

import scalaz.std.scalaFuture.futureInstance
import scalaz.syntax.bind.ToBindOps

class InstrumentalArtistStorageTest extends AsyncFreeSpec with StorageSetup {
  override protected val config = TestModuleConfiguration()
  override protected def storage = config.injector.instance[InstrumentalArtistStorage]
  private val artistName = "foo"

  "store and load" in {
    storage.load(artistName).shouldEventuallyReturnNone() >>
        storage.store(artistName) >>
        storage.load(artistName).mapValue(_ shouldReturn ())
  }
  "delete" in {
    storage.store(artistName) >> storage.delete(artistName).run >>
        storage.load(artistName).shouldEventuallyReturnNone()
  }
}
