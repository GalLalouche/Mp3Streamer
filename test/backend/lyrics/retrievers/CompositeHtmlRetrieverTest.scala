package backend.lyrics.retrievers

import backend.Url
import backend.logging.Logger
import backend.lyrics.Instrumental
import backend.module.TestModuleConfiguration
import common.AuxSpecs
import common.rich.RichFuture._
import common.rich.RichT._
import models.{FakeModelFactory, Song}
import net.codingwell.scalaguice.InjectorExtensions._
import org.scalatest.{FreeSpec, OneInstancePerTest}

import scala.concurrent.{ExecutionContext, Future}

class CompositeHtmlRetrieverTest extends FreeSpec with AuxSpecs with OneInstancePerTest {
  private val injector = TestModuleConfiguration().injector
  private implicit val ec: ExecutionContext = injector.instance[ExecutionContext]
  private class FakeLyricsRetriever(
      songsToFind: Song, urlToMatch: Url, instrumentalText: String) extends HtmlRetriever {
    var numberOfTimesInvoked = 0 // Mockito spy is throwing NPE for some reason
    override def apply(s: Song) = parse(urlToMatch, s)
    override def doesUrlMatchHost(url: Url) = {
      numberOfTimesInvoked += 1
      urlToMatch == url
    }
    override def parse = {
      numberOfTimesInvoked += 1
      (url: Url, s: Song) =>
        if (url == null || s == null)
          Future.successful(null)
        else
          Future.successful(Instrumental(instrumentalText))
              .filter((s == songsToFind).const)
              .filter((url == urlToMatch).const)
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
  private val $ = new CompositeHtmlRetriever(ec, injector.instance[Logger], Vector(r1, r2, r3))

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
      $(song2).get shouldReturn Instrumental("bar")
      r3.numberOfTimesInvoked shouldReturn 0
    }
    "when none of the URLs match" in {
      $(unfoundSong).getFailure shouldBe a[NoSuchElementException]
    }
  }
  "parse" - {
    "when one of the URLs match it doesn't try to the others" in {
      $.parse(Url("bar"), song2).get shouldReturn Instrumental("bar")
      r3.numberOfTimesInvoked shouldReturn 0
    }
    "when none of the URLs match" in {
      $(unfoundSong).getFailure shouldBe a[NoSuchElementException]
    }
  }
}
