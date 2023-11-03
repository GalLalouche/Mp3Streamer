package backend.lyrics.retrievers

import scalaz.syntax.bind.ToBindOps

import org.scalatest.AsyncFreeSpec

import backend.module.TestModuleConfiguration
import backend.recon.{Artist, ArtistReconStorage, StoredReconResult}
import backend.StorageSetup
import common.rich.func.BetterFutureInstances._
import net.codingwell.scalaguice.InjectorExtensions._

class SlickInstrumentalArtistStorageTest extends AsyncFreeSpec with StorageSetup {
  protected override val config = TestModuleConfiguration()
  val injector = config.injector
  protected override def storage: InstrumentalArtistStorage =
    injector.instance[SlickInstrumentalArtistStorage]
  private val artistName = "foo"

  override def beforeEach() = {
    val artistStorage = injector.instance[ArtistReconStorage]
    artistStorage.utils.clearOrCreateTable() >>
      artistStorage.store(Artist(artistName), StoredReconResult.NoRecon) >>
      super.beforeEach()
  }

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
