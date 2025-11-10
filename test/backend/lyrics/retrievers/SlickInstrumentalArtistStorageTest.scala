package backend.lyrics.retrievers

import backend.StorageSetup
import backend.module.TestModuleConfiguration
import backend.recon.{Artist, ArtistReconStorage, StoredReconResult}
import net.codingwell.scalaguice.InjectorExtensions._
import org.scalatest.freespec.AsyncFreeSpec

import cats.implicits.catsSyntaxFlatMapOps

class SlickInstrumentalArtistStorageTest extends AsyncFreeSpec with StorageSetup {
  protected override val config = TestModuleConfiguration()
  private val injector = config.injector
  protected override def storage: InstrumentalArtistStorage =
    injector.instance[SlickInstrumentalArtistStorage]
  private val artistName = "foo"
  private val artist = Artist(artistName)

  override def beforeEach() = {
    val artistStorage = injector.instance[ArtistReconStorage]
    artistStorage.utils.clearOrCreateTable() >>
      artistStorage.store(artist, StoredReconResult.StoredNull) >>
      super.beforeEach()
  }

  "store and load" in {
    storage.load(artist).shouldEventuallyReturnNone() >>
      storage.store(artist) >>
      storage.load(artist).mapValue(_ shouldReturn ())
  }
  "delete" in {
    storage.store(artist) >> storage.delete(artist).value >>
      storage.load(artist).shouldEventuallyReturnNone()
  }
}
