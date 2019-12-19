package backend.external

import java.time.LocalDateTime

import backend._
import backend.module.TestModuleConfiguration
import backend.recon.{Album, Artist}
import backend.storage.{AlwaysFresh, DatedFreshness}
import net.codingwell.scalaguice.InjectorExtensions._
import org.scalatest.AsyncFreeSpec
import org.scalatest.OptionValues._

import scalaz.std.scalaFuture.futureInstance
import scalaz.syntax.bind.ToBindOps

class AlbumExternalStorageTest extends AsyncFreeSpec with StorageSetup {
  override protected val config: TestModuleConfiguration = new TestModuleConfiguration
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
    storage.store(album, value).>>(storage.load(album)).map(_.value shouldReturn value)
  }
  "No problem with an empty list" in {
    storage.store(album, Nil -> AlwaysFresh) >>
        storage.load(album).map(_.value shouldReturn(Nil, AlwaysFresh))
  }
  "Can force store" in {
    val link = MarkedLink[Album](
      Url("www.foobar.com/foo/bar.html"), Host("foobar", Url("www.foobar.com")), LinkMark.New)
    storage.store(album, Nil -> AlwaysFresh)
        .>>(storage.forceStore(album, Vector(link) -> DatedFreshness(LocalDateTime.now)))
        .map(_.value shouldReturn (Nil -> AlwaysFresh))
  }
  "Delete all links by artist" in {
    val value1 = Vector(link1) -> AlwaysFresh
    val album2 = album.copy(title = "sophomore effort")
    val value2 = Vector(link2) -> DatedFreshness(LocalDateTime.now)
    val expected = Vector((album.normalize, value1._1, value1._2), (album2.normalize, value2._1, value2._2))
    storage.store(album, value1) >> storage.store(album2, value2) >>
        storage.deleteAllLinks(album.artist).map(_ shouldMultiSetEqual expected) >>
        storage.load(album).map(_ shouldReturn None) >>
        storage.load(album2).map(_ shouldReturn None)
  }

  "Can handle links with ';' in their text" in {
    val link4 = MarkedLink[Album](
      Url("www.bazqux.com/baz/quxlt&;.html"), Host("annoying", Url("annoying.com")), LinkMark.New)
    val value = Vector(link1, link2, link3, link4) -> DatedFreshness(LocalDateTime.now)
    storage.store(album, value) >> storage.load(album).map(_.value shouldReturn value)
  }
}
