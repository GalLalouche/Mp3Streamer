package server

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

  private val mp3Song1 = mf.copySong(factory.song(
    filePath = "song1.mp3",
    title = "Song One",
    artistName = "TestArtist",
    albumName = "TestAlbum",
    trackNumber = 1,
    year = 2020,
  ))
  private val mp3Song2 = mf.copySong(factory.song(
    filePath = "song2.mp3",
    title = "Song Two",
    artistName = "TestArtist",
    albumName = "TestAlbum",
    trackNumber = 2,
    year = 2020,
  ))
  private val flacSong = mf.copySong(factory.song(
    filePath = "song3.flac",
    title = "Song Three",
    artistName = "TestArtist",
    albumName = "TestAlbum",
    trackNumber = 3,
    year = 2020,
  ))
  private val disc1Song = mf.copySong(factory.song(
    filePath = "disc1_song.mp3",
    title = "Disc 1 Song",
    artistName = "DiscArtist",
    albumName = "DiscAlbum",
    trackNumber = 1,
    year = 2021,
    discNumber = Some("1"),
  ))
  private val disc2Song = mf.copySong(factory.song(
    filePath = "disc2_song.mp3",
    title = "Disc 2 Song",
    artistName = "DiscArtist",
    albumName = "DiscAlbum",
    trackNumber = 2,
    year = 2021,
    discNumber = Some("2"),
  ))

  private def encodedPath(song: MemorySong): String =
    java.net.URLEncoder.encode(song.file.path, "UTF-8")
  private def albumPath(song: MemorySong): String = song.file.parent.path

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
    val encodedAlbumPath = java.net.URLEncoder.encode(albumPath(mp3Song1), "UTF-8")
    getJson(Uri.unsafeParse(s"data/album/$encodedAlbumPath")).map { json =>
      val songs = json.as[JsArray]
      songs.value should have size 3
      (songs(0) \ "title").as[String] shouldReturn "Song One"
      (songs(1) \ "title").as[String] shouldReturn "Song Two"
      (songs(2) \ "title").as[String] shouldReturn "Song Three"
    }
  }

  "disc returns songs filtered by disc number 1" in {
    val encodedDiscPath = java.net.URLEncoder.encode(albumPath(disc1Song), "UTF-8")
    getJson(Uri.unsafeParse(s"data/disc/1/$encodedDiscPath")).map { json =>
      val songs = json.as[JsArray]
      songs.value should have size 1
      (songs(0) \ "title").as[String] shouldReturn "Disc 1 Song"
    }
  }

  "disc returns songs filtered by disc number 2" in {
    val encodedDiscPath = java.net.URLEncoder.encode(albumPath(disc2Song), "UTF-8")
    getJson(Uri.unsafeParse(s"data/disc/2/$encodedDiscPath")).map { json =>
      val songs = json.as[JsArray]
      songs.value should have size 1
      (songs(0) \ "title").as[String] shouldReturn "Disc 2 Song"
    }
  }

  "song returns the requested song" in {
    val encoded = encodedPath(mp3Song1)
    getJson(Uri.unsafeParse(s"data/song/$encoded")).map { json =>
      (json \ "title").as[String] shouldReturn "Song One"
      (json \ "artistName").as[String] shouldReturn "TestArtist"
      (json \ "albumName").as[String] shouldReturn "TestAlbum"
      (json \ "track").as[Int] shouldReturn 1
      (json \ "year").as[Int] shouldReturn 2020
    }
  }

  "nextSong returns the following track" in {
    val encoded = encodedPath(mp3Song1)
    getJson(Uri.unsafeParse(s"data/nextSong/$encoded")).map { json =>
      (json \ "title").as[String] shouldReturn "Song Two"
      (json \ "track").as[Int] shouldReturn 2
    }
  }
}
