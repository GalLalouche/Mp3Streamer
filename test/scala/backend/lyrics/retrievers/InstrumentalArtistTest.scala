package backend.lyrics.retrievers

import backend.configs.TestConfiguration
import backend.lyrics.Instrumental
import common.AuxSpecs
import common.rich.RichFuture._
import org.scalatest.{BeforeAndAfter, FreeSpec}
import search.FakeModelFactory

class InstrumentalArtistTest extends FreeSpec with AuxSpecs with BeforeAndAfter {
  private implicit val c = TestConfiguration()
  private val factory = new FakeModelFactory
  private val storage = new InstrumentalArtistStorage
  private val $ = new InstrumentalArtist
  storage.utils.createTable().get

  after {
    storage.utils.clearTable().get
  }

  "exists" in {
    val song = factory.song(artistName = "foo")
    storage.store("foo").get
    $.find(song).get shouldReturn Instrumental("Default for Artist")
  }
  "doesn't exist" in {
    val song = factory.song(artistName = "foo")
    storage.store("bar").get
    $.find(song).getFailure
  }
}
