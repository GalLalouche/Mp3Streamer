package server

import com.google.inject.Module
import models.FakeModelFactory
import musicfinder.FakeMusicFiles
import net.codingwell.scalaguice.InjectorExtensions._
import play.api.libs.json.JsObject
import sttp.client3.UriContext

import common.json.RichJson._
import common.rich.func.kats.ToMoreApplyOps.toMoreApplyOps
import common.test.memory_ref.MemoryRoot

private class SearchTest(serverModule: Module) extends HttpServerSpecs(serverModule) {
  override protected def overridingModule: Module = FakeScoreModule.module

  private val factory = new FakeModelFactory(injector.instance[MemoryRoot])
  private val mf = injector.instance[FakeMusicFiles]

  mf.copySong(factory.song(
    title = "Bohemian Rhapsody",
    artistName = "Queen",
    albumName = "Night at the Opera",
  ))
  mf.copySong(factory.song(
    title = "Stairway to Heaven",
    artistName = "Led Zeppelin",
    albumName = "Led Zeppelin IV",
  ))

  // Indexing is lazy: triggered once, memoized via Future.
  private lazy val indexed = getString(uri"index/index")

  "search by song title" in {
    indexed *>> getJson(uri"search/Bohemian").map { result =>
      result.as[JsObject].keys shouldReturn Set("songs", "albums", "artists")
      result.array("songs").value.head.str("title") shouldReturn "Bohemian Rhapsody"
    }
  }

  "search by artist name" in {
    indexed *>> getJson(uri"search/Queen").map(_.array("artists").value.head.str("name") shouldReturn "Queen")
  }

  "search with no results returns empty arrays" in {
    indexed *>> getJson(uri"search/nonexistentterm").map { result =>
      result.array("songs").value shouldBe empty
      result.array("albums").value shouldBe empty
      result.array("artists").value shouldBe empty
    }
  }

}
