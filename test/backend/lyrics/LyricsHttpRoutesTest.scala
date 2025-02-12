package backend.lyrics

import backend.lyrics.retrievers.InstrumentalArtistStorage
import backend.module.FakeWSResponse
import backend.recon.{Artist, ArtistReconStorage, StoredReconResult}
import cats.effect.unsafe.implicits.global
import http4s.Http4sSpecs
import io.lemonlabs.uri.Url
import models.{IOSong, Song}
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import org.http4s.Status
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FreeSpec, OneInstancePerTest}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.tags.Slow

import scala.concurrent.ExecutionContext

import common.rich.func.BetterFutureInstances._
import scalaz.syntax.bind.ToBindOps

import common.{MutablePartialFunction, RichUrl}
import common.rich.RichFuture._
import common.rich.path.RichFile.richFile

@Slow
class LyricsHttpRoutesTest
    extends FreeSpec
    with MockitoSugar
    with Http4sSpecs
    with BeforeAndAfterAll
    with BeforeAndAfter
    with OneInstancePerTest {
  // Modified by some tests
  private val urlToResponseMapper = MutablePartialFunction.empty[Url, FakeWSResponse]
  private implicit lazy val iec: ExecutionContext = injector.instance[ExecutionContext]
  protected override def baseTestModule =
    super.baseTestModule.copy(_urlToResponseMapper = urlToResponseMapper)

  lazy val song: Song = IOSong.read(getResourceFile("/models/song.mp3"))
  lazy val encodedSong: String = encoder(song.file.path)

  before {
    val artistStorage = injector.instance[ArtistReconStorage]
    (artistStorage.utils.clearOrCreateTable() >>
      artistStorage.store(Artist(song.artistName), StoredReconResult.NoRecon) >>
      injector.instance[LyricsStorage].utils.createTable() >>
      injector.instance[InstrumentalArtistStorage].utils.createTable()).get
  }

  private def getLyricsForSong: String = get[String]("lyrics/" + encodedSong)

  "get" in {
    injector
      .instance[LyricsStorage]
      .store(song, HtmlLyrics("foo", "bar", LyricsUrl.Url(Url.parse("http://foo.com"))))
      .get
    getLyricsForSong shouldReturn """bar<br><br>Source: <a href="http://foo.com" target="_blank">foo</a>"""
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
