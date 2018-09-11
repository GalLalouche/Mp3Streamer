package backend.recon

import backend.StorageSetup
import backend.module.TestModuleConfiguration
import backend.recon.StoredReconResult.{HasReconResult, NoRecon}
import common.AuxSpecs
import common.rich.RichFuture._
import net.codingwell.scalaguice.InjectorExtensions._
import org.scalatest.{FreeSpec, Matchers}

import scala.concurrent.ExecutionContext

class AlbumReconStorageTest extends FreeSpec with AuxSpecs
    with Matchers with StorageSetup {
  override protected val config = new TestModuleConfiguration
  private implicit val ec: ExecutionContext = config.injector.instance[ExecutionContext]
  override protected def storage = config.injector.instance[AlbumReconStorage]

  "deleteAllRecons" - {
    "No previous data" in {
      storage.deleteAllRecons(Artist("foobar")).get shouldBe empty
    }
    "has previous data" in {
      val artist: Artist = Artist("bar")
      val album1: Album = Album("foo", 2000, artist)
      val album2: Album = Album("spam", 2001, artist)
      val album3: Album = Album("eggs", 2002, artist)
      storage.store(album1, HasReconResult(ReconID("recon1"), true)).get
      storage.store(album2, HasReconResult(ReconID("recon2"), false)).get
      storage.store(album3, NoRecon).get
      storage.deleteAllRecons(artist).get.toSet shouldReturn Set(
        ("bar - foo", Some(ReconID("recon1")), true),
        ("bar - spam", Some(ReconID("recon2")), false),
        ("bar - eggs", None, true))
      storage.load(album1).get shouldReturn None
      storage.load(album2).get shouldReturn None
      storage.load(album3).get shouldReturn None
    }
  }
}
