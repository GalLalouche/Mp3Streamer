package backend.lyrics

import backend.lyrics.retrievers.InstrumentalArtistStorage
import backend.module.{FakeWSResponse, TestModuleConfiguration}
import backend.recon.{Artist, ArtistReconStorage, StoredReconResult}
import cats.effect.unsafe.implicits.global
import com.google.inject.Module
import http4s.Http4sSpecs
import io.lemonlabs.uri.Url
import models.{IOSong, Song}
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import org.http4s.Status
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FreeSpec}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.tags.Slow

import scala.concurrent.ExecutionContext

import common.rich.func.BetterFutureInstances._
import scalaz.syntax.bind.ToBindOps

import common.{MutablePartialFunction, RichUrl}
import common.guice.RichModule.richModule
import common.rich.RichFuture._
import common.rich.path.RichFile.richFile

@Slow
class LyricsHttpRoutesTest
    extends FreeSpec
    with MockitoSugar
    with Http4sSpecs
    with BeforeAndAfterAll
    with BeforeAndAfter {
  // Modified by some tests
  private val urlToResponseMapper = MutablePartialFunction.empty[Url, FakeWSResponse]
  private implicit lazy val iec: ExecutionContext = injector.instance[ExecutionContext]
  protected override def module: Module =
    super.module.overrideWith(
      TestModuleConfiguration(_urlToResponseMapper = urlToResponseMapper).module,
    )

  lazy val song: Song = IOSong.read(getResourceFile("/models/song.mp3"))
  lazy val encodedSong: String = encode(song.file.path)
  protected override def beforeAll(): Unit = {
    super.beforeAll()
    val artistStorage = injector.instance[ArtistReconStorage]
    (artistStorage.utils.clearOrCreateTable() >>
      artistStorage.store(Artist(song.artistName), StoredReconResult.NoRecon) >>
      injector.instance[LyricsStorage].utils.createTable() >>
      injector.instance[InstrumentalArtistStorage].utils.createTable()).get
  }
  before {
    urlToResponseMapper.clear()
  }

  after {
    (injector.instance[LyricsStorage].utils.clearTable() >>
      injector.instance[InstrumentalArtistStorage].utils.clearTable()).get
  }

  private def getLyricsForSong: String = get[String]("lyrics/" + encodedSong)

  "get" in {
    injector
      .instance[LyricsStorage]
      .store(song, HtmlLyrics("foo", "bar", LyricsUrl.Url(Url.parse("http://foo.com"))))
      .get
    get[String](
      "lyrics/" + encodedSong,
    ) shouldReturn """bar<br><br>Source: <a href="http://foo.com" target="_blank">foo</a>"""
  }

  "push" in {
    injector
      .instance[LyricsStorage]
      .store(song, HtmlLyrics("foo", "bar", LyricsUrl.Url(Url.parse("http://foo.com"))))
      .get
    urlToResponseMapper += { case RichUrl.Unapplied("https://www.azlyrics.com/lyrics/Foobar") =>
      FakeWSResponse(bytes = getResourceFile("/backend/lyrics/retrievers/az_lyrics.html").bytes)
    }
    post[String, String](
      "lyrics/push/" + encodedSong,
      "https://www.azlyrics.com/lyrics/Foobar",
    ).unsafeRunSync() should startWith("Ascending in sectarian rapture")
    getLyricsForSong should startWith("Ascending in sectarian rapture")
  }

  "setInstrumentalSong" in {
    post[String]("lyrics/instrumental/song/" + encodedSong)
      .unsafeRunSync() shouldReturn Htmls.InstrumentalSongHtml
    getLyricsForSong shouldReturn Htmls.InstrumentalSongHtml
  }

  "setInstrumentalArtist" in {
    // Make all HTML retrievers fail
    urlToResponseMapper.const(FakeWSResponse(status = Status.NotFound.code))
    post[String](
      "lyrics/instrumental/artist/" + encodedSong,
    ).unsafeRunSync() shouldReturn Htmls.InstrumentalArtistHtml
    getLyricsForSong shouldReturn Htmls.InstrumentalArtistHtml
  }
}
