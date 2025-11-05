package backend.lyrics.retrievers

import backend.StorageSetup
import backend.module.TestModuleConfiguration
import backend.recon.{Artist, ArtistReconStorage, StoredReconResult}
import cats.implicits.catsSyntaxFlatMapOps
import net.codingwell.scalaguice.InjectorExtensions._
import org.scalatest.freespec.AsyncFreeSpec

class SlickInstrumentalArtistStorageTest extends AsyncFreeSpec with StorageSetup {
  protected override val config = TestModuleConfiguration()
  private val injector = config.injector
  protected override def storage: InstrumentalArtistStorage =
    injector.instance[SlickInstrumentalArtistStorage]
  private val artistName = "foo"

  override def beforeEach() = {
    val artistStorage = injector.instance[ArtistReconStorage]
    artistStorage.utils.clearOrCreateTable() >>
      artistStorage.store(Artist(artistName), StoredReconResult.StoredNull) >>
      super.beforeEach()
  }

  "store and load" in {
    storage.load(artistName).shouldEventuallyReturnNone() >>
      storage.store(artistName) >>
      storage.load(artistName).mapValue(_ shouldReturn ())
  }
  "delete" in {
    storage.store(artistName) >> storage.delete(artistName).value >>
      storage.load(artistName).shouldEventuallyReturnNone()
  }
}
