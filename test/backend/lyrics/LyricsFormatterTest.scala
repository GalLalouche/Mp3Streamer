package backend.lyrics

import backend.lyrics.retrievers.{InstrumentalArtistStorage, RetrievedLyricsResult}
import backend.module.{FakeWSResponse, TestModuleConfiguration}
import backend.Url
import backend.recon.{Artist, ArtistReconStorage, StoredReconResult}
import controllers.UrlPathUtils
import models.{IOSong, Song}
import net.codingwell.scalaguice.InjectorExtensions._
import org.mockito.Mockito.when
import org.scalatest.AsyncFreeSpec
import org.scalatest.mockito.MockitoSugar
import play.api.http.Status

import scala.concurrent.{ExecutionContext, Future}

import scalaz.syntax.bind.ToBindOps
import common.rich.func.BetterFutureInstances._

import common.rich.path.RichFile._
import common.storage.Storage
import common.test.{AsyncAuxSpecs, BeforeAndAfterEachAsync}
import common.MutablePartialFunction

class LyricsFormatterTest extends AsyncFreeSpec with BeforeAndAfterEachAsync with AsyncAuxSpecs with MockitoSugar {
  // Modified by some tests
  private val urlToResponseMapper = MutablePartialFunction.empty[Url, FakeWSResponse]
  private val injector = TestModuleConfiguration(_urlToResponseMapper = urlToResponseMapper).injector
  private val $ = injector.instance[LyricsFormatter]
  private val song: Song = IOSong.read(getResourceFile("/models/song.mp3"))
  private val encodedSong: String = injector.instance[UrlPathUtils] encodePath song

  private def setup(s: Storage[_, _]) = s.utils.clearOrCreateTable()
  override def beforeEach(): Future[_] = {
    val artistStorage = injector.instance[ArtistReconStorage]
    artistStorage.utils.clearOrCreateTable() >>
        artistStorage.store(Artist(song.artistName), StoredReconResult.NoRecon) >>
        setup(injector.instance[InstrumentalArtistStorage]) >>
        setup(injector.instance[LyricsStorage])
  }

  private def getLyricsForSong: Future[String] = $.get(encodedSong)

  "get" in {
    injector.instance[LyricsStorage].store(song, HtmlLyrics("foo", "bar")) >>
        $.get(encodedSong) shouldEventuallyReturn "bar<br><br>Source: foo"
  }

  "push" - {
    "success" in {
      urlToResponseMapper += {
        case Url("http://lyrics.wikia.com/wiki/Foobar") =>
          FakeWSResponse(bytes = getResourceFile("/backend/lyrics/retrievers/lyrics_wikia_lyrics.html").bytes)
      }
      injector.instance[LyricsStorage].store(song, HtmlLyrics("foo", "bar")) >>
          $.push(encodedSong, Url("http://lyrics.wikia.com/wiki/Foobar"))
              .map(_ should startWith("Daddy's flown across the ocean")) >>
          getLyricsForSong.map(_ should startWith("Daddy's flown across the ocean"))
    }
    "failure returns the actual error" in {
      val cache = mock[LyricsCache]
      when(cache.parse(Url("bar"), song))
          .thenReturn(Future.successful(RetrievedLyricsResult.Error(new Exception("Oopsy <daisy>"))))
      new LyricsFormatter(
        injector.instance[ExecutionContext],
        cache,
        injector.instance[UrlPathUtils],
      )
          .push(encodedSong, Url("bar")) shouldEventuallyReturn "Oopsy &lt;daisy&gt;"
    }
  }

  "setInstrumentalSong" in {
    $.setInstrumentalSong(encodedSong).shouldEventuallyReturn(Htmls.InstrumentalSongHtml) >>
        getLyricsForSong.shouldEventuallyReturn(Htmls.InstrumentalSongHtml)
  }

  "setInstrumentalArtist" in {
    // Make all HTML retrievers fail
    urlToResponseMapper const FakeWSResponse(status = Status.NOT_FOUND)
    $.setInstrumentalArtist(encodedSong).shouldEventuallyReturn(Htmls.InstrumentalArtistHtml) >>
        getLyricsForSong.shouldEventuallyReturn(Htmls.InstrumentalArtistHtml)
  }
}
