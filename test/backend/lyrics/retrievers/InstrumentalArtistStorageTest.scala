package backend.lyrics.retrievers

import backend.StorageSetup
import backend.configs.TestConfiguration
import common.AuxSpecs
import common.rich.RichFuture._
import org.scalatest.FreeSpec

class InstrumentalArtistStorageTest extends FreeSpec with AuxSpecs with StorageSetup {
  override protected implicit val config = TestConfiguration()
  override protected def storage = new InstrumentalArtistStorage
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
