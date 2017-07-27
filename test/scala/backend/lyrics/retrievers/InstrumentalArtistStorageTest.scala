package backend.lyrics.retrievers

import backend.configs.TestConfiguration
import common.AuxSpecs
import common.rich.RichFuture._
import org.scalatest.{BeforeAndAfter, FreeSpec}

class InstrumentalArtistStorageTest extends FreeSpec with AuxSpecs with BeforeAndAfter {
  private implicit val c = TestConfiguration()
  private val $ = new InstrumentalArtistStorage
  private val artistName = "foo"
  $.utils.createTable().get

  after {
    $.utils.clearTable().get
  }

  "store and load" in {
    $.load(artistName).get shouldReturn None
    $.store(artistName).get
    $.load(artistName).get shouldReturn Some(())
  }
  "delete" in {
    $.store(artistName).get
    $.delete(artistName).get
    $.load(artistName).get shouldReturn None
  }
}
