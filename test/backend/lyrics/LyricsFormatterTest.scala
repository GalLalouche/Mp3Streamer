package backend.lyrics

import backend.lyrics.retrievers.InstrumentalArtistStorage
import backend.module.{FakeWSResponse, TestModuleConfiguration}
import backend.Url
import controllers.UrlPathUtils
import models.{IOSong, Song}
import net.codingwell.scalaguice.InjectorExtensions._
import org.scalatest.{AsyncFreeSpec, BeforeAndAfterAll, BeforeAndAfterEach}
import play.api.http.Status

import scala.concurrent.Future

import scalaz.std.scalaFuture.futureInstance
import scalaz.syntax.bind.ToBindOps

import common.{AsyncAuxSpecs, MutablePartialFunction}
import common.rich.path.RichFile._
import common.storage.Storage

class LyricsFormatterTest extends AsyncFreeSpec with BeforeAndAfterAll with BeforeAndAfterEach
    with AsyncAuxSpecs {
  // Modified by some tests
  private val urlToResponseMapper = MutablePartialFunction.empty[Url, FakeWSResponse]
  private val injector = TestModuleConfiguration(_urlToResponseMapper = urlToResponseMapper).injector
  private val $ = injector.instance[LyricsFormatter]
  private val song: Song = IOSong.read(getResourceFile("/models/song.mp3"))
  private val encodedSong: String = injector.instance[UrlPathUtils] encodePath song

  private def setup(s: Storage[_, _]) = s.utils.clearOrCreateTable()
  override def withFixture(test: NoArgAsyncTest) = runBefore(
    setup(injector.instance[InstrumentalArtistStorage]) >> setup(injector.instance[LyricsStorage])
  )(test)

  private def getLyricsForSong: Future[String] = $.get(encodedSong)

  "get" in {
    injector.instance[LyricsStorage].store(song, HtmlLyrics("foo", "bar")) >>
        $.get(encodedSong).map(_ shouldReturn "bar<br><br>Source: foo")
  }

  "push" in {
    urlToResponseMapper += {
      case Url("http://lyrics.wikia.com/wiki/Foobar") =>
        FakeWSResponse(bytes = getResourceFile("/backend/lyrics/retrievers/lyrics_wikia_lyrics.html").bytes)
    }
    injector.instance[LyricsStorage].store(song, HtmlLyrics("foo", "bar")) >>
        $.push(encodedSong, Url("http://lyrics.wikia.com/wiki/Foobar"))
            .map(_ should startWith("Daddy's flown across the ocean")) >>
        getLyricsForSong.map(_ should startWith("Daddy's flown across the ocean"))
  }

  "setInstrumentalSong" in {
    $.setInstrumentalSong(encodedSong).map(_ shouldReturn Htmls.InstrumentalSongHtml) >>
        getLyricsForSong.map(_ shouldReturn Htmls.InstrumentalSongHtml)
  }

  "setInstrumentalArtist" in {
    // Make all HTML retrievers fail
    urlToResponseMapper const FakeWSResponse(status = Status.NOT_FOUND)
    $.setInstrumentalArtist(encodedSong).map(_ shouldReturn Htmls.InstrumentalArtistHtml) >>
        getLyricsForSong.map(_ shouldReturn Htmls.InstrumentalArtistHtml)
  }
}
