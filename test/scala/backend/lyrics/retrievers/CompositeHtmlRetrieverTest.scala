package backend.lyrics.retrievers

import backend.Url
import backend.configs.TestConfiguration
import backend.lyrics.Instrumental
import common.AuxSpecs
import common.rich.RichFuture._
import common.rich.RichT._
import models.Song
import org.mockito.{Matchers, Mockito}
import org.scalatest.mock.MockitoSugar
import org.scalatest.{FreeSpec, OneInstancePerTest}
import search.FakeModelFactory

import scala.concurrent.Future

class CompositeHtmlRetrieverTest extends FreeSpec with AuxSpecs with OneInstancePerTest with MockitoSugar {
  private def fakeLyricsRetriever(
      songsToFind: Song, urlToMatch: Url, instrumentalText: String): HtmlRetriever = {
    class FakeLyricsRetriever extends HtmlRetriever {
      override def find(s: Song) = parse(urlToMatch, s)
      override def doesUrlMatchHost(url: Url) = url == urlToMatch
      override def parse(url: Url, s: Song) =
        Future.successful(Instrumental(instrumentalText))
            .filter(s.==(songsToFind).const)
            .filter(url.==(urlToMatch).const)
    }
    Mockito.spy(new FakeLyricsRetriever)
  }
  private implicit val c = TestConfiguration()
  private val factory = new FakeModelFactory
  private val song1 = factory.song(title = "song 1")
  private val song2 = factory.song(title = "song 2")
  private val song3 = factory.song(title = "song 3")
  private val unfoundSong = factory.song(title = "unfound song")

  private val r1 = fakeLyricsRetriever(song1, Url("foo"), "foo")
  private val r2 = fakeLyricsRetriever(song2, Url("bar"), "bar")
  private val r3 = fakeLyricsRetriever(song3, Url("bazz"), "quxx")
  private val $ = new CompositeHtmlRetriever(r1, r2, r3)

  "doesUrlMatch" - {
    "when one of the URLs match" in {
      $ doesUrlMatchHost Url("bazz") shouldReturn true
    }
    "when non match" in {
      $ doesUrlMatchHost Url("moo") shouldReturn false
    }
  }
  "find" - {
    "when one of the URLs match" in {
      $.find(song2).get shouldReturn Instrumental("bar")
      Mockito.verify(r3, Mockito.never()).find(Matchers.any())
    }
    "when none of the URLs match" in {
      $.find(unfoundSong).getFailure shouldBe a[NoSuchElementException]
    }
  }
  "parse" - {
    "when one of the URLs match" in {
      $.parse(Url("bar"), song2).get shouldReturn Instrumental("bar")
      Mockito.verify(r3, Mockito.never()).parse(Matchers.any(), Matchers.any())
    }
    "when none of the URLs match" in {
      $.find(unfoundSong).getFailure shouldBe a[NoSuchElementException]
    }
  }
}
