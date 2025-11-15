package backend.lyrics

import backend.lyrics.LyricsUrl.ManualEmpty
import backend.lyrics.retrievers._
import backend.module.TestModuleConfiguration
import backend.recon.ArtistReconStorage
import io.lemonlabs.uri.Url
import models.FakeModelFactory
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import org.mockito.Mockito.when
import org.scalatest.OneInstancePerTest
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatestplus.mockito.MockitoSugar

import scala.concurrent.{ExecutionContext, Future}

import cats.implicits.catsSyntaxFlatMapOps

import common.rich.RichT.lazyT
import common.test.{AsyncAuxSpecs, BeforeAndAfterAllAsync}

class LyricsCacheTest
    extends AsyncFreeSpec
    with AsyncAuxSpecs
    with MockitoSugar
    with OneInstancePerTest
    with BeforeAndAfterAllAsync {
  private val injector = TestModuleConfiguration().injector
  implicit override val executionContext: ExecutionContext = injector.instance[ExecutionContext]
  private val factory = new FakeModelFactory

  private val lyricsStorage = injector.instance[LyricsStorage]
  private val activeRetriever = mock[ActiveRetriever]
  private val passiveParser: PassiveParser = mock[PassiveParser]

  private val song = factory.song(artistName = "Instrumental Artist", title = "Some Song")

  protected override def beforeAll(): Future[_] =
    artistStorage.utils.clearOrCreateTable() >> lyricsStorage.utils.clearOrCreateTable()

  private val $ = new LyricsCache(
    executionContext,
    activeRetriever,
    passiveParser,
    lyricsStorage,
  )

  private val artistStorage = injector.instance[ArtistReconStorage]
  private val url = Url.parse("http://www.test.com")
  private val lyrics = Instrumental("Some lyrics", LyricsUrl.Url(url))

  "find" - {
    "when lyrics are already cached, returns them from cache" in {
      lyricsStorage.store(song, lyrics) >> $.find(song).shouldEventuallyReturn(lyrics)
    }

    "when lyrics are not cached, retrieves them from retrievers and caches them" in {
      when(activeRetriever.apply(song))
        .thenReturn(Future.successful(RetrievedLyricsResult.RetrievedLyrics(lyrics)))

      $.find(song).shouldEventuallyReturn(lyrics) >>
        lyricsStorage.load(song).valueShouldEventuallyReturn(lyrics)
    }

    "when all retrievers fail, returns an error" in {
      when(activeRetriever.apply(song))
        .thenReturn(Future.successful(RetrievedLyricsResult.NoLyrics))

      $.find(song).checkFailure(_.getMessage should include("No lyrics retrieved"))
    }
  }

  "setInstrumentalSong" in {
    val instrumental = Instrumental("Manual override", ManualEmpty)
    $.setInstrumentalSong(song)
      .shouldEventuallyReturn(instrumental)
      .>>(lyricsStorage.load(song).valueShouldEventuallyReturn(instrumental))
  }

  "parse" - {
    "writes to storage on success" in {
      val retrievedLyrics = RetrievedLyricsResult.RetrievedLyrics(lyrics)
      when(passiveParser.parse).thenReturn(Future.successful(retrievedLyrics).const2)
      when(passiveParser.doesUrlMatchHost).thenReturn(_ == url)

      $.parse(url, song).shouldEventuallyReturn(retrievedLyrics) >>
        lyricsStorage.load(song).valueShouldEventuallyReturn(lyrics)
    }

    "No available parser" in {
      when(passiveParser.doesUrlMatchHost).thenReturn(false.const)

      $.parse(url, song).map { case RetrievedLyricsResult.Error(e) =>
        e.getMessage shouldReturn RetrievedLyricsResult.Error.unsupportedHost(url).e.getMessage
      }
    }
  }
}
