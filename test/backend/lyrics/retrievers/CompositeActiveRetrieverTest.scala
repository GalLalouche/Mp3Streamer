package backend.lyrics.retrievers

import backend.lyrics.{Instrumental, LyricsUrl}
import io.lemonlabs.uri.Url
import models.FakeModelFactory
import org.scalatest.OneInstancePerTest
import org.scalatest.freespec.AsyncFreeSpec

import common.rich.func.kats.ToMoreFunctorOps.toMoreFunctorOps

import common.test.AsyncAuxSpecs

class CompositeActiveRetrieverTest
    extends AsyncFreeSpec
    with AsyncAuxSpecs
    with OneInstancePerTest {
  private val factory = new FakeModelFactory
  private val song1 = factory.song(title = "song 1")
  private val song2 = factory.song(title = "song 2")
  private val song3 = factory.song(title = "song 3")
  private val unfoundSong = factory.song(title = "unfound song")

  private val r1 = new FakeAmphiRetriever(song1, Url("foo"), "foo")
  private val r2 = new FakeAmphiRetriever(song2, Url("bar"), "bar")
  private val r3 = new FakeAmphiRetriever(song3, Url("bazz"), "quxx")
  private val $ = new CompositeActiveRetriever(Vector(r1, r2, r3))(executionContext)

  "find" - {
    "when one of the retrievers worked, shouldn't check all the subsequent ones" in {
      val l = RetrievedLyricsResult.RetrievedLyrics(Instrumental("bar", LyricsUrl.Url(Url("bar"))))
      $(song2).shouldEventuallyReturn(l) >| r3.numberOfTimesInvoked.shouldReturn(0)
    }
    "when none of the URLs match" in {
      $(unfoundSong) shouldEventuallyReturn RetrievedLyricsResult.NoLyrics
    }
  }
}
