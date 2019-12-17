package backend.external

import java.time.LocalDateTime

import backend._
import backend.module.TestModuleConfiguration
import backend.recon.{Album, Artist}
import backend.storage.{AlwaysFresh, DatedFreshness}
import net.codingwell.scalaguice.InjectorExtensions._
import org.scalatest.FreeSpec

import scala.concurrent.ExecutionContext

import common.AuxSpecs
import common.rich.RichFuture._

class AlbumExternalStorageTest extends FreeSpec with AuxSpecs with StorageSetup {
  override protected val config: TestModuleConfiguration = new TestModuleConfiguration
  private implicit val ec: ExecutionContext = config.injector.instance[ExecutionContext]
  override protected val storage = config.injector.instance[AlbumExternalStorage]

  private val album: Album = Album("the spam album", 2000, Artist("foo and the bar band"))
  private val link1 = MarkedLink[Album](
    Url("www.foobar.com/foo/bar.html"), Host("foobar", Url("www.foobar.com")), LinkMark.New)
  private val link2 = MarkedLink[Album](
    Url("www.bazqux.com/baz/qux.html"), Host("bazqux", Url("www.bazqux.com")), LinkMark.None)
  private val link3 = MarkedLink[Album](
    Url("www.spam.com/eggs/ni.html"), Host("bazqux", Url("www.spam.com")), LinkMark.Missing)

  "Can load what is stored" in {
    val value = Vector(link1, link2, link3) -> DatedFreshness(LocalDateTime.now)
    storage.store(album, value).get
    storage.load(album).get.get shouldReturn value
  }
  "No problem with an empty list" in {
    storage.store(album, Nil -> AlwaysFresh).get
    storage.load(album).get.get._1 shouldReturn Nil
    storage.load(album).get.get._2 shouldReturn AlwaysFresh
  }
  "Can force store" in {
    storage.store(album, Nil -> AlwaysFresh).get
    val link1 = MarkedLink[Album](Url("www.foobar.com/foo/bar.html"), Host("foobar", Url("www.foobar.com")), LinkMark.New)
    storage.forceStore(album, Vector(link1) -> DatedFreshness(LocalDateTime.now)).get.get shouldReturn (Nil -> AlwaysFresh)
  }
  "Delete all links by artist" in {
    val value1 = Vector(link1) -> AlwaysFresh
    storage.store(album, value1).get
    val album2 = album.copy(title = "sophomore effort")
    val value2 = Vector(link2) -> DatedFreshness(LocalDateTime.now)
    storage.store(album2, value2).get
    storage.deleteAllLinks(album.artist).get.toSet shouldReturn
        Set((album.normalize, value1._1, value1._2), (album2.normalize, value2._1, value2._2))
    storage.load(album).get shouldReturn None
    storage.load(album2).get shouldReturn None
  }

  "Can handle links with ';' in their text" in {
    val link4 = MarkedLink[Album](Url("www.bazqux.com/baz/quxlt&;.html"), Host("annoying", Url("annoying.com")), LinkMark.New)
    val value = Vector(link1, link2, link3, link4) -> DatedFreshness(LocalDateTime.now)
    storage.store(album, value).get
    storage.load(album).get.get shouldReturn value
  }
}
