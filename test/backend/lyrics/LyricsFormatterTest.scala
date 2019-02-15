package backend.lyrics

import backend.lyrics.retrievers.InstrumentalArtistStorage
import backend.module.{FakeWSResponse, TestModuleConfiguration}
import backend.Url
import common.{AuxSpecs, MutablePartialFunction}
import common.rich.path.RichFile._
import common.rich.RichFuture._
import controllers.UrlPathUtils
import models.{IOSong, Song}
import net.codingwell.scalaguice.InjectorExtensions._
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FreeSpec}
import org.scalatest.mockito.MockitoSugar
import play.api.http.Status

import scala.concurrent.ExecutionContext

import scalaz.std.FutureInstances
import scalaz.syntax.ToBindOps

class LyricsFormatterTest extends FreeSpec with MockitoSugar
    with BeforeAndAfterAll with BeforeAndAfter
    with ToBindOps with FutureInstances
    with AuxSpecs {
  // Modified by some tests
  private val urlToResponseMapper = MutablePartialFunction.empty[Url, FakeWSResponse]
  private val injector = TestModuleConfiguration(_urlToResponseMapper = urlToResponseMapper).injector
  private val $ = injector.instance[LyricsFormatter]
  private implicit val ec: ExecutionContext = injector.instance[ExecutionContext]
  private val song: Song = IOSong.read(getResourceFile("/models/song.mp3"))
  private val encodedSong: String = injector.instance[UrlPathUtils] encodePath song

  override protected def beforeAll(): Unit = {
    (injector.instance[LyricsStorage].utils.createTable() >>
        injector.instance[InstrumentalArtistStorage].utils.createTable()).get
  }

  before {
    urlToResponseMapper.clear()
  }

  after {
    (injector.instance[LyricsStorage].utils.clearTable() >>
        injector.instance[InstrumentalArtistStorage].utils.clearTable()).get
  }

  private def getLyricsForSong: String = $.get(encodedSong).get

  "get" in {
    injector.instance[LyricsStorage].store(song, HtmlLyrics("foo", "bar")).get
    $.get(encodedSong).get shouldReturn "bar<br><br>Source: foo"
  }

  "push" in {
    injector.instance[LyricsStorage].store(song, HtmlLyrics("foo", "bar")).get
    urlToResponseMapper += {
      case Url("http://lyrics.wikia.com/wiki/Foobar") =>
        FakeWSResponse(bytes = getResourceFile("/backend/lyrics/retrievers/lyrics_wikia_lyrics.html").bytes)
    }
    $.push(encodedSong, Url("http://lyrics.wikia.com/wiki/Foobar"))
        .get should startWith("Daddy's flown across the ocean")
    getLyricsForSong should startWith("Daddy's flown across the ocean")
  }

  "setInstrumentalSong" in {
    val InstrumentalSongHtml =
      "<img src='assets/images/TrebleClef.png' width='30' height='68' /><b>Instrumental</b><br><br>Source: Manual override"
    $.setInstrumentalSong(encodedSong).get shouldReturn InstrumentalSongHtml
    getLyricsForSong shouldReturn InstrumentalSongHtml
  }

  "setInstrumentalArtist" in {
    // Make all HTML retrievers fail
    urlToResponseMapper const FakeWSResponse(status = Status.NOT_FOUND)
    val InstrumentalArtistHtml =
      "<img src='assets/images/TrebleClef.png' width='30' height='68' /><b>Instrumental</b><br><br>Source: Default for artist"
    $.setInstrumentalArtist(encodedSong).get shouldReturn InstrumentalArtistHtml
    getLyricsForSong shouldReturn InstrumentalArtistHtml
  }
}
