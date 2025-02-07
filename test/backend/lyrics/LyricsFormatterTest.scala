package backend.lyrics

import backend.lyrics.retrievers.{InstrumentalArtistStorage, RetrievedLyricsResult}
import backend.module.{FakeWSResponse, TestModuleConfiguration}
import backend.recon.{Artist, ArtistReconStorage, StoredReconResult}
import io.lemonlabs.uri.Url
import models.{IOSong, Song}
import net.codingwell.scalaguice.InjectorExtensions._
import org.mockito.Mockito.when
import org.scalatest.{AsyncFreeSpec, OneInstancePerTest}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.tags.Slow
import play.api.http.Status

import scala.concurrent.{ExecutionContext, Future}

import common.rich.func.BetterFutureInstances._
import scalaz.syntax.bind.ToBindOps

import common.{MutablePartialFunction, RichUrl}
import common.rich.path.RichFile._
import common.storage.Storage
import common.test.{AsyncAuxSpecs, BeforeAndAfterEachAsync}

@Slow
class LyricsFormatterTest
    extends AsyncFreeSpec
    with BeforeAndAfterEachAsync
    with AsyncAuxSpecs
    with MockitoSugar
    with OneInstancePerTest {
  // Modified by some tests
  private val urlToResponseMapper = MutablePartialFunction.empty[Url, FakeWSResponse]
  private val injector =
    TestModuleConfiguration(_urlToResponseMapper = urlToResponseMapper).injector
  private val $ = injector.instance[LyricsFormatter]
  private val song: Song = IOSong.read(getResourceFile("/models/song.mp3"))
  private val songPath: String = song.file.path

  private def setup(s: Storage[_, _]) = s.utils.clearOrCreateTable()
  override def beforeEach(): Future[_] = {
    val artistStorage = injector.instance[ArtistReconStorage]
    artistStorage.utils.clearOrCreateTable() >>
      artistStorage.store(Artist(song.artistName), StoredReconResult.NoRecon) >>
      setup(injector.instance[InstrumentalArtistStorage]) >>
      setup(injector.instance[LyricsStorage])
  }

  private def getLyricsForSong: Future[String] = $.get(songPath)

  "get" - {
    "With URL" in {
      val expected = """bar<br><br>Source: <a href="http://bazz.com" target="_blank">foo</a>"""
      injector
        .instance[LyricsStorage]
        .store(song, HtmlLyrics("foo", "bar", LyricsUrl.Url(Url.parse("http://bazz.com")))) >>
        $.get(songPath) shouldEventuallyReturn expected
    }
    def testCaseObject(lu: LyricsUrl): Unit =
      ("With " + lu.toString) in {
        injector.instance[LyricsStorage].store(song, HtmlLyrics("foo", "bar", lu)) >>
          $.get(songPath) shouldEventuallyReturn "bar<br><br>Source: foo"
      }
    def isUrl: LyricsUrl => Boolean = {
      case LyricsUrl.Url(_) => true
      case _ => false
    }
    LyricsUrl.values.filterNot(isUrl).foreach(testCaseObject)
  }

  "push" - {
    "success" in {
      urlToResponseMapper += { case RichUrl.Unapplied("https://www.azlyrics.com/lyrics/Foobar") =>
        FakeWSResponse(bytes = getResourceFile("/backend/lyrics/retrievers/az_lyrics.html").bytes)
      }
      injector
        .instance[LyricsStorage]
        .store(
          song,
          HtmlLyrics(
            "foo",
            "bar",
            LyricsUrl.Url(Url.parse("https://www.azlyrics.com/lyrics/Foobar")),
          ),
        ) >>
        $.push(songPath, Url.parse("https://www.azlyrics.com/lyrics/Foobar"))
          .map(_ should startWith("Ascending in sectarian rapture")) >>
        getLyricsForSong.map(_ should startWith("Ascending in sectarian rapture"))
    }
    "failure returns the actual error" in {
      val cache = mock[LyricsCache]
      when(cache.parse(Url.parse("bar"), song))
        .thenReturn(Future.successful(RetrievedLyricsResult.Error(new Exception("Oopsy <daisy>"))))
      new LyricsFormatter(injector.instance[ExecutionContext], cache)
        .push(songPath, Url.parse("bar")) shouldEventuallyReturn "Oopsy &lt;daisy&gt;"
    }
  }

  "setInstrumentalSong" in {
    $.setInstrumentalSong(songPath).shouldEventuallyReturn(Htmls.InstrumentalSongHtml) >>
      getLyricsForSong.shouldEventuallyReturn(Htmls.InstrumentalSongHtml)
  }

  "setInstrumentalArtist" in {
    // Make all HTML retrievers fail
    urlToResponseMapper.const(FakeWSResponse(status = Status.NOT_FOUND))
    $.setInstrumentalArtist(songPath).shouldEventuallyReturn(Htmls.InstrumentalArtistHtml) >>
      getLyricsForSong.shouldEventuallyReturn(Htmls.InstrumentalArtistHtml)
  }
}
