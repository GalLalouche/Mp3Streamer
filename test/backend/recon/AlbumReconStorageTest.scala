package backend.recon

import backend.StorageSetup
import net.codingwell.scalaguice.InjectorExtensions._
import backend.configs.TestConfiguration
import common.AuxSpecs
import common.rich.RichFuture._
import org.scalatest.{FreeSpec, Matchers}

import scala.concurrent.ExecutionContext

class AlbumReconStorageTest extends FreeSpec with AuxSpecs
    with Matchers with StorageSetup {
  override protected implicit val config: TestConfiguration = new TestConfiguration
  private implicit val ec: ExecutionContext = config.injector.instance[ExecutionContext]
  override protected def storage = new AlbumReconStorage

  "deleteAllRecons" - {
    "No previous data" in {
      storage.deleteAllRecons(Artist("foobar")).get shouldBe empty
    }
    "has previous data" in {
      val artist: Artist = Artist("bar")
      val album1: Album = Album("foo", 2000, artist)
      val album2: Album = Album("spam", 2001, artist)
      val album3: Album = Album("eggs", 2002, artist)
      storage.store(album1, Some(ReconID("recon1")) -> true).get
      storage.store(album2, Some(ReconID("recon2")) -> false).get
      storage.store(album3, None -> true).get
      storage.deleteAllRecons(artist).get.toSet shouldReturn
          Set(("bar - foo", Some(ReconID("recon1")), true), ("bar - spam", Some(ReconID("recon2")), false), ("bar - eggs", None, true))
      storage.load(album1).get shouldReturn None
      storage.load(album2).get shouldReturn None
      storage.load(album3).get shouldReturn None
    }
  }
}
