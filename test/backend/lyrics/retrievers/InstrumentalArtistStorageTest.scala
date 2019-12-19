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
    storage.load(artistName).map(_ shouldReturn None) >>
        storage.store(artistName) >>
        storage.load(artistName).map(_ shouldReturn Some())
  }
  "delete" in {
    storage.store(artistName) >> storage.delete(artistName) >> storage.load(artistName).map(_ shouldBe 'empty)
  }
}
