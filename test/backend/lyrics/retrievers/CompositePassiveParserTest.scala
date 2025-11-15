package backend.lyrics.retrievers

import backend.lyrics.{Instrumental, LyricsUrl}
import io.lemonlabs.uri.Url
import models.FakeModelFactory
import org.scalatest.OneInstancePerTest
import org.scalatest.freespec.AsyncFreeSpec

import common.rich.func.kats.ToMoreFunctorOps.toMoreFunctorOps

import common.test.AsyncAuxSpecs

class CompositePassiveParserTest extends AsyncFreeSpec with AsyncAuxSpecs with OneInstancePerTest {
  private val factory = new FakeModelFactory
  private val song1 = factory.song(title = "song 1")
  private val song2 = factory.song(title = "song 2")
  private val song3 = factory.song(title = "song 3")

  private val unmatchedUrl = Url.parse("http://unmatched.com/unmatched")

  private val p1 = new FakeAmphiRetriever(song1, Url.parse("http://example.com/foo"), "foo")
  private val p2 = new FakeAmphiRetriever(song2, Url.parse("http://example.com/bar"), "bar")
  private val p3 = new FakeAmphiRetriever(song3, Url.parse("http://example.com/bazz"), "quxx")
  private val $ = new CompositePassiveParser(Vector(p1, p2, p3))(executionContext)

  "doesUrlMatchHost" - {
    "returns true when one of the parsers matches" in {
      $.doesUrlMatchHost(Url.parse("http://example.com/bar")) shouldReturn true
    }
    "returns false when no parser matches" in {
      $.doesUrlMatchHost(unmatchedUrl) shouldReturn false
    }
  }

  "parse" - {
    "when one of the parsers matches, shouldn't check all the subsequent ones" in {
      val l = RetrievedLyricsResult.RetrievedLyrics(
        Instrumental("bar", LyricsUrl.Url(Url.parse("http://example.com/bar"))),
      )
      $.parse(Url.parse("http://example.com/bar"), song2)
        .shouldEventuallyReturn(l) >| p3.numberOfTimesInvoked.shouldReturn(0)
    }
    "when none of the URLs match" in {
      val expectedMsg = RetrievedLyricsResult.Error.unsupportedHost(unmatchedUrl).e.getMessage
      val unfoundSong = factory.song(title = "unfound song")
      $.parse(unmatchedUrl, unfoundSong).map { case RetrievedLyricsResult.Error(e) =>
        e.getMessage shouldReturn expectedMsg
      }
    }
  }
}
