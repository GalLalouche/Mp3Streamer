package backend.external

import java.time.LocalDateTime

import backend.StorageSetup
import backend.module.TestModuleConfiguration
import backend.recon.{Album, Artist}
import backend.storage.{AlwaysFresh, DatedFreshness}
import io.lemonlabs.uri.Url
import net.codingwell.scalaguice.InjectorExtensions._
import org.scalatest.AsyncFreeSpec

import common.rich.func.BetterFutureInstances._
import scalaz.syntax.bind.ToBindOps

private class SlickAlbumExternalStorageTest extends AsyncFreeSpec with StorageSetup {
  protected override val config: TestModuleConfiguration = new TestModuleConfiguration
  protected override val storage: AlbumExternalStorage =
    config.injector.instance[SlickAlbumExternalStorage]

  private val album: Album = Album("the spam album", 2000, Artist("foo and the bar band"))
  private val link1 = MarkedLink[Album](
    Url.parse("www.foobar.com/foo/bar.html"),
    Host("foobar", Url.parse("www.foobar.com")),
    LinkMark.New,
  )
  private val link2 = MarkedLink[Album](
    Url.parse("www.bazqux.com/baz/qux.html"),
    Host("bazqux", Url.parse("www.bazqux.com")),
    LinkMark.None,
  )
  private val link3 = MarkedLink[Album](
    Url.parse("www.spam.com/eggs/ni.html"),
    Host("bazqux", Url.parse("www.spam.com")),
    LinkMark.Missing,
  )
  private val link4 = MarkedLink[Album](
    Url.parse("www.spam.com/eggs/ni.html"),
    Host("egg", Url.parse("www.grault.com")),
    LinkMark.Text("corge"),
  )

  "Can load what is stored" in {
    val value = Vector(link1, link2, link3) -> DatedFreshness(LocalDateTime.now)
    storage.store(album, value) >> storage.load(album).valueShouldEventuallyReturn(value)
  }
  "No problem with an empty list" in {
    storage.store(album, Nil -> AlwaysFresh) >>
      storage.load(album).valueShouldEventuallyReturn(Nil -> AlwaysFresh)
  }
  "Can update" in {
    val link = MarkedLink[Album](
      Url.parse("www.foobar.com/foo/bar.html"),
      Host("foobar", Url.parse("www.foobar.com")),
      LinkMark.New,
    )
    storage.store(album, Nil -> AlwaysFresh) >>
      storage
        .update(album, Vector(link) -> DatedFreshness(LocalDateTime.now))
        .valueShouldEventuallyReturn(Nil -> AlwaysFresh)
  }
  "Delete all links by artist" in {
    val value1 = Vector(link1) -> AlwaysFresh
    val album2 = new Album("sophomore effort", album.year, album.artist)
    val value2 = Vector(link2) -> DatedFreshness(LocalDateTime.now)
    val expected =
      Vector((album.normalize, value1._1, value1._2), (album2.normalize, value2._1, value2._2))
    storage.store(album, value1) >> storage.store(album2, value2) >> checkAll(
      storage.deleteAllLinks(album.artist).map(_ shouldMultiSetEqual expected),
      storage.load(album).shouldEventuallyReturnNone(),
      storage.load(album2).shouldEventuallyReturnNone(),
    )
  }

  "Can handle links with ';' in their text" in {
    val link5 = MarkedLink[Album](
      Url.parse("www.bazqux.com/baz/quxlt&;.html"),
      Host("annoying", Url.parse("annoying.com")),
      LinkMark.New,
    )
    val value = Vector(link1, link2, link3, link4, link5) -> DatedFreshness(LocalDateTime.now)
    storage.store(album, value) >> storage.load(album).valueShouldEventuallyReturn(value)
  }
  "canonicalizes host on extraction" in {
    val nonStandardWikipediaLink = MarkedLink[Album](
      Url.parse("en.wikipedia.org/foo/bar.html"),
      Host("Wikipedia", Url.parse("en.wikipedia.org")),
      LinkMark.New,
    )
    val standardWikipediaLink =
      MarkedLink[Album](Url.parse("en.wikipedia.org/foo/bar.html"), Host.Wikipedia, LinkMark.New)
    storage.store(album, Vector(nonStandardWikipediaLink) -> AlwaysFresh) >>
      storage.load(album).valueShouldEventuallyReturn(Vector(standardWikipediaLink) -> AlwaysFresh)
  }
}
