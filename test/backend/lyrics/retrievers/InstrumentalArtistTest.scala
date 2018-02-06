package backend.lyrics.retrievers

import backend.StorageSetup
import backend.configs.TestConfiguration
import backend.lyrics.Instrumental
import common.AuxSpecs
import common.rich.RichFuture._
import models.FakeModelFactory
import org.scalatest.FreeSpec

class InstrumentalArtistTest extends FreeSpec with AuxSpecs with StorageSetup {
  override protected implicit val config = TestConfiguration()
  override protected lazy val storage = new InstrumentalArtistStorage
  private val factory = new FakeModelFactory
  private val $ = new InstrumentalArtist

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
