package backend.external

import java.time.LocalDateTime

import backend._
import backend.configs.TestConfiguration
import backend.recon.{Album, Artist}
import common.AuxSpecs
import common.rich.RichFuture._
import net.codingwell.scalaguice.InjectorExtensions._
import org.scalatest.FreeSpec

import scala.concurrent.ExecutionContext

class AlbumExternalStorageTest extends FreeSpec with AuxSpecs with StorageSetup {
  override protected implicit val config: TestConfiguration = new TestConfiguration
  private implicit val ec: ExecutionContext = config.injector.instance[ExecutionContext]
  override protected val storage = new AlbumExternalStorage()

  private val album: Album = Album("the spam album", 2000, Artist("foo and the bar band"))
  private val link1 = MarkedLink[Album](Url("www.foobar.com/foo/bar.html"), Host("foobar", Url("www.foobar.com")), true)
  private val link2 = MarkedLink[Album](Url("www.bazqux.com/baz/qux.html"), Host("bazqux", Url("www.bazqux.com")), false)

  "Can load what is stored" in {
    val value = List(link1, link2) -> Some(LocalDateTime.now)
    storage.store(album, value).get
    storage.load(album).get.get shouldReturn value
  }
  "No problem with an empty list" in {
    storage.store(album, Nil -> None).get
    storage.load(album).get.get._1 shouldReturn Nil
    storage.load(album).get.get._2 shouldReturn None
  }
  "Can force store" in {
    storage.store(album, Nil -> None).get
    val link1 = MarkedLink[Album](Url("www.foobar.com/foo/bar.html"), Host("foobar", Url("www.foobar.com")), true)
    storage.forceStore(album, List(link1) -> Some(LocalDateTime.now)).get.get shouldReturn (Nil -> None)
  }
  "Delete all links by artist" in {
    val value1: (List[MarkedLink[Album]], None.type) = List(link1) -> None
    storage.store(album, value1).get
    val album2 = album.copy(title = "sophomore effort")
    val value2: (List[MarkedLink[Album]], Some[LocalDateTime]) = List(link2) -> Some(LocalDateTime.now)
    storage.store(album2, value2).get
    storage.deleteAllLinks(album.artist).get.toSet shouldReturn
        Set((album.normalize, value1._1, value1._2), (album2.normalize, value2._1, value2._2))
    storage.load(album).get shouldReturn None
    storage.load(album2).get shouldReturn None
  }
}
