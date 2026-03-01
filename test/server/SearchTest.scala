package server

import com.google.inject.Module
import models.FakeModelFactory
import musicfinder.FakeMusicFiles
import net.codingwell.scalaguice.InjectorExtensions._
import play.api.libs.json.JsObject
import sttp.client3.UriContext

import common.rich.func.kats.ToMoreApplyOps.toMoreApplyOps

import common.json.RichJson._
import common.rich.collections.RichTraversableOnce._
import common.test.memory_ref.MemoryRoot

private class SearchTest(serverModule: Module) extends HttpServerSpecs(serverModule) {
  protected override def overridingModule: Module = FakeScoreModule.module

  private val factory = new FakeModelFactory(injector.instance[MemoryRoot])
  private val mf = injector.instance[FakeMusicFiles]

  mf.copySong(
    factory.song(
      title = "Bohemian Rhapsody",
      artistName = "Queen",
      albumName = "Night at the Opera",
    ),
  )
  mf.copySong(
    factory.song(
      title = "Stairway to Heaven",
      artistName = "Led Zeppelin",
      albumName = "Led Zeppelin IV",
    ),
  )

  private def indexed = getString(uri"index/index")

  "search by song title" in {
    indexed *>> getJson(uri"search/Bohemian").map { result =>
      result.as[JsObject].keys shouldReturn Set("songs", "albums", "artists")
      val song = result.array("songs").value.single
      song.str("title") shouldReturn "Bohemian Rhapsody"
      song.str("artistName") shouldReturn "Queen"
      song.str("albumName") shouldReturn "Night at the Opera"
      song.int("track") shouldReturn 1
      song.int("year") shouldReturn 2000
    }
  }

  "search by artist name" in {
    indexed *>> getJson(uri"search/Queen").map { result =>
      val artist = result.array("artists").value.single
      artist.str("name") shouldReturn "Queen"
      val album = artist.objects("albums").single
      album.str("title") shouldReturn "Night at the Opera"
      album.str("artistName") shouldReturn "Queen"
      album.int("year") shouldReturn 2000
      val song = album.array("songs").value.single
      song.str("title") shouldReturn "Bohemian Rhapsody"
      song.str("artistName") shouldReturn "Queen"
      song.str("albumName") shouldReturn "Night at the Opera"
    }
  }

  "search with no results returns empty arrays" in {
    indexed *>> getJson(uri"search/nonexistentterm").map { result =>
      result.array("songs").value shouldBe empty
      result.array("albums").value shouldBe empty
      result.array("artists").value shouldBe empty
    }
  }
}
