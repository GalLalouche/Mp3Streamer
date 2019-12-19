package backend.recon

import backend.StorageSetup
import backend.module.TestModuleConfiguration
import backend.recon.StoredReconResult.{HasReconResult, NoRecon}
import net.codingwell.scalaguice.InjectorExtensions._
import org.scalatest.AsyncFreeSpec

import scala.concurrent.ExecutionContext

import scalaz.std.scalaFuture.futureInstance
import scalaz.syntax.bind.ToBindOpsUnapply

class AlbumReconStorageTest extends AsyncFreeSpec with StorageSetup {
  override protected val config = new TestModuleConfiguration
  private implicit val ec: ExecutionContext = config.injector.instance[ExecutionContext]
  override protected def storage = config.injector.instance[AlbumReconStorage]

  "deleteAllRecons" - {
    "No previous data" in {
      storage.deleteAllRecons(Artist("foobar")).map(_ shouldBe empty)
    }
    "has previous data" in {
      val artist: Artist = Artist("bar")
      val album1: Album = Album("foo", 2000, artist)
      val album2: Album = Album("spam", 2001, artist)
      val album3: Album = Album("eggs", 2002, artist)
      storage.store(album1, HasReconResult(ReconID("recon1"), true)) >>
          storage.store(album2, HasReconResult(ReconID("recon2"), false)) >>
          storage.store(album3, NoRecon) >>
          storage.deleteAllRecons(artist).map(_ shouldMultiSetEqual Set(
            ("bar - foo", Some(ReconID("recon1")), true),
            ("bar - spam", Some(ReconID("recon2")), false),
            ("bar - eggs", None, true),
          )) >>
          storage.load(album1).map(_ shouldBe 'empty) >>
          storage.load(album2).map(_ shouldBe 'empty) >>
          storage.load(album3).map(_ shouldBe 'empty)
    }
  }
}
