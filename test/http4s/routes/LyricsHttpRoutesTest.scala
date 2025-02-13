package http4s.routes

import backend.lyrics.LyricsBridge
import backend.module.FakeWSResponse
import cats.effect.unsafe.implicits.global
import io.lemonlabs.uri.Url
import models.{IOSong, Song}
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import org.http4s.Status
import org.scalatest.{BeforeAndAfter, FreeSpec, OneInstancePerTest}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.tags.Slow

import scala.concurrent.ExecutionContext

import common.{MutablePartialFunction, RichUrl}
import common.rich.RichFuture.richFuture
import common.rich.path.RichFile.richFile

@Slow
private class LyricsHttpRoutesTest
    extends FreeSpec
    with MockitoSugar
    with Http4sSpecs
    with BeforeAndAfter
    with OneInstancePerTest {
  // Modified by some tests
  private val urlToResponseMapper = MutablePartialFunction.empty[Url, FakeWSResponse]
  private implicit lazy val iec: ExecutionContext = injector.instance[ExecutionContext]
  protected override def baseTestModule =
    super.baseTestModule.copy(_urlToResponseMapper = urlToResponseMapper)

  private lazy val song: Song = IOSong.read(getResourceFile("/models/song.mp3"))
  private lazy val encodedSong: String = encoder(song.file.path)
  private lazy val bridge = injector.instance[LyricsBridge]

  before {
    bridge.setup(song).get
  }

  private def getLyricsForSong: String = get[String]("lyrics/" + encodedSong)

  "get" in {
    bridge.store(song, "foo", "bar", Url.parse("http://foo.com")).get
    getLyricsForSong shouldReturn """bar<br><br>Source: <a href="http://foo.com" target="_blank">foo</a>"""
  }

  "push" in {
    bridge.store(song, "foo", "bar", Url.parse("http://foo.com")).get
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
