package backend.lyrics

import backend.lyrics.retrievers.InstrumentalArtistStorage
import backend.module.{FakeWSResponse, TestModuleConfiguration}
import backend.Url
import backend.external.DocumentSpecs
import backend.recon.{Artist, ArtistReconStorage, StoredReconResult}
import controllers.ControllerSpec
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FreeSpec}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.tags.Slow
import play.api.http.Status
import play.api.inject.guice.GuiceApplicationBuilder

import scalaz.syntax.bind.ToBindOps
import common.rich.func.BetterFutureInstances._

import common.rich.RichFuture._
import common.MutablePartialFunction

@Slow
class LyricsControllerTest extends FreeSpec with MockitoSugar with ControllerSpec with DocumentSpecs
    with BeforeAndAfterAll with BeforeAndAfter {
  // Modified by some tests
  private val urlToResponseMapper = MutablePartialFunction.empty[Url, FakeWSResponse]
  override def fakeApplication() = GuiceApplicationBuilder()
      .overrides(TestModuleConfiguration(_urlToResponseMapper = urlToResponseMapper).module)
      .build

  private val inj = app.injector
  override protected def beforeAll(): Unit = {
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
    inj.instanceOf[LyricsStorage].store(song, HtmlLyrics("foo", "bar")).get
    get("lyrics/" + encodedSong).getString shouldReturn "bar<br><br>Source: foo"
  }

  "push" in {
    inj.instanceOf[LyricsStorage].store(song, HtmlLyrics("foo", "bar")).get
    urlToResponseMapper += {
      case Url("http://lyrics.wikia.com/wiki/Foobar") =>
        FakeWSResponse(bytes = getBytes("/backend/lyrics/retrievers/lyrics_wikia_lyrics.html"))
    }
    post("lyrics/push/" + encodedSong, "http://lyrics.wikia.com/wiki/Foobar")
        .getString should startWith("Daddy's flown across the ocean")
    getLyricsForSong should startWith("Daddy's flown across the ocean")
  }

  "setInstrumentalSong" in {
    post("lyrics/instrumental/song/" + encodedSong).getString shouldReturn Htmls.InstrumentalSongHtml
    getLyricsForSong shouldReturn Htmls.InstrumentalSongHtml
  }

  "setInstrumentalArtist" in {
    // Make all HTML retrievers fail
    urlToResponseMapper const FakeWSResponse(status = Status.NOT_FOUND)
    post("lyrics/instrumental/artist/" + encodedSong).getString shouldReturn Htmls.InstrumentalArtistHtml
    getLyricsForSong shouldReturn Htmls.InstrumentalArtistHtml
  }
}
