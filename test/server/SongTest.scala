package server

import java.net.URLEncoder

import com.google.inject.Module
import models.{FakeModelFactory, MemorySong}
import musicfinder.FakeMusicFiles
import net.codingwell.scalaguice.InjectorExtensions._
import play.api.libs.json.{JsArray, JsObject, JsValue}
import sttp.model.Uri

import common.test.memory_ref.MemoryRoot

private class SongTest(serverModule: Module) extends HttpServerSpecs(serverModule) {
  private val factory = new FakeModelFactory(injector.instance[MemoryRoot])
  private val mf = injector.instance[FakeMusicFiles]

  private val mp3Song1 = mf.copySong(
    factory.song(
      filePath = "song1.mp3",
      title = "Song One",
      artistName = "TestArtist",
      albumName = "TestAlbum",
      trackNumber = 1,
      year = 2020,
    ),
  )
  mf.copySong(
    factory.song(
      filePath = "song2.mp3",
      title = "Song Two",
      artistName = "TestArtist",
      albumName = "TestAlbum",
      trackNumber = 2,
      year = 2020,
    ),
  )
  mf.copySong(
    factory.song(
      filePath = "song3.flac",
      title = "Song Three",
      artistName = "TestArtist",
      albumName = "TestAlbum",
      trackNumber = 3,
      year = 2020,
    ),
  )
  private val disc1Song = mf.copySong(
    factory.song(
      filePath = "disc1_song.mp3",
      title = "Disc 1 Song",
      artistName = "DiscArtist",
      albumName = "DiscAlbum",
      trackNumber = 1,
      year = 2021,
      discNumber = Some("1"),
    ),
  )
  private val disc2Song = mf.copySong(
    factory.song(
      filePath = "disc2_song.mp3",
      title = "Disc 2 Song",
      artistName = "DiscArtist",
      albumName = "DiscAlbum",
      trackNumber = 2,
      year = 2021,
      discNumber = Some("2"),
    ),
  )

  private def encode(path: String): String = URLEncoder.encode(path, "UTF-8")
  private def encodedPath(song: MemorySong): String = encode(song.file.path)
  private def encodedAlbumPath(song: MemorySong): String = encode(song.file.parent.path)

  "randomSong returns a valid song" in {
    getJson(Uri.unsafeParse("data/randomSong")).map { json =>
      val obj = json.as[JsObject]
      val knownTitles = Set("Song One", "Song Two", "Song Three", "Disc 1 Song", "Disc 2 Song")
      knownTitles should contain((obj \ "title").as[String])
    }
  }

  "randomSong/mp3 returns an mp3 song" in {
    getJson(Uri.unsafeParse("data/randomSong/mp3")).map { json =>
      json.as[JsObject].keys should contain("mp3")
    }
  }

  "randomSong/flac returns a flac song" in {
    getJson(Uri.unsafeParse("data/randomSong/flac")).map { json =>
      json.as[JsObject].keys should contain("flac")
    }
  }

  "album returns all songs in the album" in {
    getJson(Uri.unsafeParse(s"data/album/${encodedAlbumPath(mp3Song1)}")).map { json =>
      val songs = json.as[JsArray]
      songs.value should have size 3
      (songs(0) \ "title").as[String] shouldReturn "Song One"
      (songs(1) \ "title").as[String] shouldReturn "Song Two"
      (songs(2) \ "title").as[String] shouldReturn "Song Three"
    }
  }

  "disc returns songs filtered by disc number 1" in {
    getJson(Uri.unsafeParse(s"data/disc/1/${encodedAlbumPath(disc1Song)}")).map { json =>
      val songs = json.as[JsArray]
      songs.value should have size 1
      (songs(0) \ "title").as[String] shouldReturn "Disc 1 Song"
    }
  }

  "disc returns songs filtered by disc number 2" in {
    getJson(Uri.unsafeParse(s"data/disc/2/${encodedAlbumPath(disc2Song)}")).map { json =>
      val songs = json.as[JsArray]
      songs.value should have size 1
      (songs(0) \ "title").as[String] shouldReturn "Disc 2 Song"
    }
  }

  "song returns the requested song" in {
    getJson(Uri.unsafeParse(s"data/song/${encodedPath(mp3Song1)}")).map { json =>
      (json \ "title").as[String] shouldReturn "Song One"
      (json \ "artistName").as[String] shouldReturn "TestArtist"
      (json \ "albumName").as[String] shouldReturn "TestAlbum"
      (json \ "track").as[Int] shouldReturn 1
      (json \ "year").as[Int] shouldReturn 2020
    }
  }

  "nextSong returns the following track" in {
    getJson(Uri.unsafeParse(s"data/nextSong/${encodedPath(mp3Song1)}")).map { json =>
      (json \ "title").as[String] shouldReturn "Song Two"
      (json \ "track").as[Int] shouldReturn 2
    }
  }
}
