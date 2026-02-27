package server

import com.google.inject.Module
import models.FakeModelFactory
import musicfinder.FakeMusicFiles
import net.codingwell.scalaguice.InjectorExtensions._
import play.api.libs.json.{JsArray, JsObject}
import sttp.client3.UriContext

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
    for {
      _ <- indexed
      result <- getJson(uri"search/Bohemian")
    } yield {
      result.as[JsObject].keys shouldReturn Set("songs", "albums", "artists")
      val songs = (result \ "songs").as[JsArray]
      songs.value should not be empty
      (songs.value.head \ "title").as[String] shouldReturn "Bohemian Rhapsody"
    }
  }

  "search by artist name" in {
    for {
      _ <- indexed
      result <- getJson(uri"search/Queen")
    } yield {
      val artists = (result \ "artists").as[JsArray]
      artists.value should not be empty
      (artists.value.head \ "name").as[String] shouldReturn "Queen"
    }
  }

  "search with no results returns empty arrays" in {
    for {
      _ <- indexed
      result <- getJson(uri"search/nonexistentterm")
    } yield {
      (result \ "songs").as[JsArray].value shouldBe empty
      (result \ "albums").as[JsArray].value shouldBe empty
      (result \ "artists").as[JsArray].value shouldBe empty
    }
  }

}
