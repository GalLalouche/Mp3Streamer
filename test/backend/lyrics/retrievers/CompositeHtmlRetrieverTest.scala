package backend.lyrics.retrievers

import scala.concurrent.Future

import org.scalatest.{AsyncFreeSpec, OneInstancePerTest}

import backend.logging.Logger
import backend.lyrics.{Instrumental, LyricsUrl}
import backend.module.TestModuleConfiguration
import backend.Url
import common.test.AsyncAuxSpecs
import models.{FakeModelFactory, Song}
import net.codingwell.scalaguice.InjectorExtensions._

class CompositeHtmlRetrieverTest extends AsyncFreeSpec with AsyncAuxSpecs with OneInstancePerTest {
  private val injector = TestModuleConfiguration().injector
  private class FakeLyricsRetriever(songsToFind: Song, urlToMatch: Url, instrumentalText: String)
      extends HtmlRetriever {
    var numberOfTimesInvoked = 0 // Mockito spy is throwing NPE for some reason
    override def get = ???
    override def apply(s: Song) = parse(urlToMatch, s)
    override def doesUrlMatchHost = url => {
      numberOfTimesInvoked += 1
      urlToMatch == url
    }
    override def parse = {
      numberOfTimesInvoked += 1
      (url: Url, s: Song) =>
        Future.successful {
          if (s == songsToFind && url == urlToMatch)
            RetrievedLyricsResult.RetrievedLyrics(
              Instrumental(instrumentalText, LyricsUrl.oldUrl(url)),
            )
          else
            RetrievedLyricsResult.NoLyrics
        }
    }
  }
  private val factory = new FakeModelFactory
  private val song1 = factory.song(title = "song 1")
  private val song2 = factory.song(title = "song 2")
  private val song3 = factory.song(title = "song 3")
  private val unfoundSong = factory.song(title = "unfound song")

  private val r1 = new FakeLyricsRetriever(song1, Url("foo"), "foo")
  private val r2 = new FakeLyricsRetriever(song2, Url("bar"), "bar")
  private val r3 = new FakeLyricsRetriever(song3, Url("bazz"), "quxx")
  private val $ =
    new CompositeHtmlRetriever(executionContext, injector.instance[Logger], Vector(r1, r2, r3))

  "doesUrlMatch" - {
    "when one of the URLs match" in {
      $.doesUrlMatchHost(Url("bazz")) shouldReturn true
    }
    "when non match" in {
      $.doesUrlMatchHost(Url("moo")) shouldReturn false
    }
  }
  "find" - {
    "when one of the URLs match, shouldn't check all the subsequent URLs" in {
      r3.numberOfTimesInvoked shouldReturn 0
      $(song2) shouldEventuallyReturn
        RetrievedLyricsResult.RetrievedLyrics(Instrumental("bar", LyricsUrl.oldUrl(Url("bar"))))
    }
    "when none of the URLs match" in {
      $(unfoundSong) shouldEventuallyReturn RetrievedLyricsResult.NoLyrics
    }
  }
  "parse" - {
    "when one of the URLs match it doesn't try to the others" in {
      r3.numberOfTimesInvoked shouldReturn 0
      $.parse(Url("bar"), song2) shouldEventuallyReturn
        RetrievedLyricsResult.RetrievedLyrics(Instrumental("bar", LyricsUrl.oldUrl(Url("bar"))))
    }
    "when none of the URLs match" in {
      $(unfoundSong) shouldEventuallyReturn RetrievedLyricsResult.NoLyrics
    }
  }
}
