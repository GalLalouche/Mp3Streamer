package backend.lyrics

import scalaz.syntax.bind.ToBindOps

import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FreeSpec}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.tags.Slow

import backend.external.DocumentSpecs
import backend.lyrics.retrievers.InstrumentalArtistStorage
import backend.module.{FakeWSResponse, TestModuleConfiguration}
import backend.recon.{Artist, ArtistReconStorage, StoredReconResult}
import backend.Url
import common.rich.func.BetterFutureInstances._
import common.rich.path.RichFile.richFile
import common.rich.RichFuture._
import common.MutablePartialFunction
import controllers.ControllerSpec
import play.api.http.Status
import play.api.inject.guice.GuiceApplicationBuilder

@Slow
class LyricsControllerTest
    extends FreeSpec
    with MockitoSugar
    with ControllerSpec
    with DocumentSpecs
    with BeforeAndAfterAll
    with BeforeAndAfter {
  // Modified by some tests
  private val urlToResponseMapper = MutablePartialFunction.empty[Url, FakeWSResponse]
  override def fakeApplication() = GuiceApplicationBuilder()
    .overrides(TestModuleConfiguration(_urlToResponseMapper = urlToResponseMapper).module)
    .build

  private val inj = app.injector
  protected override def beforeAll(): Unit = {
    val artistStorage = inj.instanceOf[ArtistReconStorage]
    (artistStorage.utils.clearOrCreateTable() >>
      artistStorage.store(Artist(song.artistName), StoredReconResult.NoRecon) >>
      inj.instanceOf[LyricsStorage].utils.createTable() >>
      inj.instanceOf[InstrumentalArtistStorage].utils.createTable()).get
  }

  before {
    urlToResponseMapper.clear()
  }

  after {
    (inj.instanceOf[LyricsStorage].utils.clearTable() >>
      inj.instanceOf[InstrumentalArtistStorage].utils.clearTable()).get
  }

  private def getLyricsForSong: String = get("lyrics/" + encodedSong).getString

  "get" in {
    inj
      .instanceOf[LyricsStorage]
      .store(song, HtmlLyrics("foo", "bar", LyricsUrl.oldUrl(Url("http://foo.com"))))
      .get
    get(
      "lyrics/" + encodedSong,
    ).getString shouldReturn """bar<br><br>Source: <a href="http://foo.com" target="_blank">foo</a>"""
  }

  "push" in {
    inj
      .instanceOf[LyricsStorage]
      .store(song, HtmlLyrics("foo", "bar", LyricsUrl.oldUrl(Url("http://foo.com"))))
      .get
    urlToResponseMapper += { case Url("https://www.azlyrics.com/lyrics/Foobar") =>
      FakeWSResponse(bytes = getResourceFile("/backend/lyrics/retrievers/az_lyrics.html").bytes)
    }
    post(
      "lyrics/push/" + encodedSong,
      "https://www.azlyrics.com/lyrics/Foobar",
    ).getString should startWith("Ascending in sectarian rapture")
    getLyricsForSong should startWith("Ascending in sectarian rapture")
  }

  "setInstrumentalSong" in {
    post(
      "lyrics/instrumental/song/" + encodedSong,
    ).getString shouldReturn Htmls.InstrumentalSongHtml
    getLyricsForSong shouldReturn Htmls.InstrumentalSongHtml
  }

  "setInstrumentalArtist" in {
    // Make all HTML retrievers fail
    urlToResponseMapper.const(FakeWSResponse(status = Status.NOT_FOUND))
    post(
      "lyrics/instrumental/artist/" + encodedSong,
    ).getString shouldReturn Htmls.InstrumentalArtistHtml
    getLyricsForSong shouldReturn Htmls.InstrumentalArtistHtml
  }
}
