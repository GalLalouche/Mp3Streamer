package songs

import backend.configs.TestConfiguration
import common.AuxSpecs
import org.scalatest.FreeSpec
import search.FakeModelFactory

import scala.search.FakeModelsJsonable

class SongGroupsTest extends FreeSpec with AuxSpecs {
  private val fakeModelFactory = new FakeModelFactory
  private val fakeJsonable = new FakeModelsJsonable
  import fakeJsonable._
  private val song1 = fakeModelFactory.song()
  private val song2 = fakeModelFactory.song()
  private val song3 = fakeModelFactory.song()
  private val song4 = fakeModelFactory.song()
  private val group1 = SongGroup(Seq(song1, song2))
  private val group2 = SongGroup(Seq(song3, song4))
  private val groups = Seq(group1, group2)
  "fromSongs" in {
    val song5 = fakeModelFactory.song()
    val $ = SongGroups.fromGroups(groups)
    $(song1) shouldReturn group1
    $(song2) shouldReturn group1
    $(song3) shouldReturn group2
    $(song4) shouldReturn group2
    $ get song5 shouldReturn None
  }
  "save and load" in {
    implicit val c = TestConfiguration()
    import c._
    val $ = new SongGroups()
    $.save(groups)
    $.load shouldReturn Set(group1, group2)
  }
}
