package backend.recon
import backend.configs.TestConfiguration
import common.AuxSpecs
import org.scalatest.{FreeSpec, OneInstancePerTest, ShouldMatchers}
import common.rich.RichFuture._

class AlbumReconStorageTest extends FreeSpec with AuxSpecs with OneInstancePerTest with ShouldMatchers {
  private implicit val c = new TestConfiguration
  private val $ = new AlbumReconStorage
  $.utils.createTable().get
  "deleteAllRecons" - {
    "No previous data" in {
      $.deleteAllRecons(Artist("foobar")).get shouldBe empty
    }
    "has previous data" in {
      val artist: Artist = Artist("bar")
      val album1: Album = Album("foo", 2000, artist)
      val album2: Album = Album("spam", 2001, artist)
      val album3: Album = Album("eggs", 2002, artist)
      $.store(album1, Some(ReconID("recon1")) -> true).get
      $.store(album2, Some(ReconID("recon2")) -> false).get
      $.store(album3, None -> true).get
      $.deleteAllRecons(artist).get.toSet shouldReturn
          Set(("bar - foo", Some(ReconID("recon1")), true), ("bar - spam", Some(ReconID("recon2")), false), ("bar - eggs", None, true))
      $.load(album1).get shouldReturn None
      $.load(album2).get shouldReturn None
      $.load(album3).get shouldReturn None
    }
  }
}
