package server

import backend.module.FakeMusicFinder
import com.google.inject.Module
import models.{AlbumDir, FakeModelFactory}
import net.codingwell.scalaguice.InjectorExtensions._
import play.api.libs.json.Json
import sttp.client3.UriContext
import sttp.model.StatusCode

import scala.concurrent.Future

import cats.Monad
import cats.implicits.toFunctorOps

import common.FakeClock
import common.io.MemoryRoot
import common.json.ToJsonableOps.parseJsValue
import common.rich.RichT.{lazyT, richT}
import common.rich.RichTime.RichClock
import common.test.BeforeAndAfterEachAsync

private class LastAlbumsTest(serverModule: Module)
    extends HttpServerSpecs(serverModule)
    with BeforeAndAfterEachAsync {
  override def afterEach(): Future[Unit] =
    Monad[Future]
      .iterateUntilM(StatusCode.Ok)(postRaw(uri"last_albums/dequeue").map(_.code).const)(
        _ == StatusCode.NotFound,
      )
      .void
  private val mj = injector.instance[models.ModelJsonable]
  import mj.albumDirJsonifier

  "get returns empty array initially" in {
    getJson(uri"last_albums") shouldEventuallyReturn Json.arr()
  }

  "get returns albums after update" in {
    val a1 = createAlbumWithSong()
    for {
      _ <- postJson(uri"last_albums/update")
      a2 = createAlbumWithSong()
      _ <- postJson(uri"last_albums/update")
      result <- getJson(uri"last_albums")
    } yield result.parse[Seq[AlbumDir]] shouldReturn Vector(a1, a2)
  }

  "dequeue returns 404 when queue is empty" in {
    postRaw(uri"last_albums/dequeue").map(_.code shouldReturn StatusCode.NotFound)
  }

  "dequeue returns next album and removes it from list" in {
    val a1 = createAlbumWithSong()
    for {
      _ <- postJson(uri"last_albums/update")
      a2 = createAlbumWithSong()
      _ <- postJson(uri"last_albums/update")
      a3 = createAlbumWithSong()
      _ <- postJson(uri"last_albums/update")
      dequeueResult <- postJson(uri"last_albums/dequeue")
      remainingAlbums <- getJson(uri"last_albums")
    } yield {
      Json.parse(dequeueResult).parse[(AlbumDir, Seq[AlbumDir])] shouldReturn (a1, Vector(a2, a3))
      remainingAlbums.parse[Seq[AlbumDir]] shouldReturn Vector(a2, a3)
    }
  }

  private val factory = new FakeModelFactory(injector.instance[MemoryRoot])
  private val mf = injector.instance[FakeMusicFinder]
  private val clock = injector.instance[FakeClock]

  private def createAlbumWithSong() = {
    clock.advance(1)
    mf.copyAlbum(factory.album(lastModified = clock.getLocalDateTime)) <| addSong
  }
  private def addSong(d: AlbumDir) =
    mf.copySong(d.dir.name, factory.song(albumName = d.title, artistName = d.artistName))
}
