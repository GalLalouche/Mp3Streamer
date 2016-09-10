package songs

import backend.configs.TestConfiguration
import common.AuxSpecs
import common.rich.RichFuture._
import org.scalatest.FreeSpec
import search.Models

class SongGroupsTest extends FreeSpec with AuxSpecs {
  private val song1 = Models.mockSong()
  private val song2 = Models.mockSong()
  private val song3 = Models.mockSong()
  private val song4 = Models.mockSong()
  private val group1 = SongGroup(Seq(song1, song2))
  private val group2 = SongGroup(Seq(song3, song4))
  private val groups = Seq(group1, group2)
  "fromSongs" in {
    val song5 = Models.mockSong()
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
    SongGroups.save(groups).get
    SongGroups.load.get.toSeq shouldReturn Seq(group1, group2)
  }
}
