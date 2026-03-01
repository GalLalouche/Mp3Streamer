package server

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

import com.google.inject.Module
import models.{FakeModelFactory, MemorySong}
import musicfinder.FakeMusicFiles
import net.codingwell.scalaguice.InjectorExtensions._
import org.scalatest.Assertion
import play.api.libs.json.{JsArray, JsObject, JsValue}
import sttp.client3.UriContext
import sttp.model.Uri

import common.json.RichJson._
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

  private def encodedPath(song: MemorySong): String =
    URLEncoder.encode(song.file.path, StandardCharsets.UTF_8)
  private def encodedAlbumPath(song: MemorySong): String =
    URLEncoder.encode(song.file.parent.path, StandardCharsets.UTF_8)

  private def verifySong(
      json: JsValue,
      title: String,
      artistName: String,
      albumName: String,
      track: Int,
      year: Int,
  ): Assertion = {
    json.str("title") shouldReturn title
    json.str("artistName") shouldReturn artistName
    json.str("albumName") shouldReturn albumName
    json.int("track") shouldReturn track
    json.int("year") shouldReturn year
  }

  "randomSong returns a valid song" in {
    getJson(uri"data/randomSong").map { json =>
      val knownTitles = Set("Song One", "Song Two", "Song Three", "Disc 1 Song", "Disc 2 Song")
      knownTitles should contain(json.str("title"))
    }
  }

  "randomSong/mp3 returns an mp3 song" in {
    getJson(uri"data/randomSong/mp3").map { json =>
      json.as[JsObject].keys should contain("mp3")
    }
  }

  "randomSong/flac returns a flac song" in {
    getJson(uri"data/randomSong/flac").map { json =>
      json.as[JsObject].keys should contain("flac")
    }
  }

  "album returns all songs in the album" in {
    getJson(Uri.unsafeParse(s"data/album/${encodedAlbumPath(mp3Song1)}")).map { json =>
      val songs = json.as[JsArray]
      songs.value should have size 3
      verifySong(songs(0), "Song One", "TestArtist", "TestAlbum", 1, 2020)
      verifySong(songs(1), "Song Two", "TestArtist", "TestAlbum", 2, 2020)
      verifySong(songs(2), "Song Three", "TestArtist", "TestAlbum", 3, 2020)
    }
  }

  "disc returns songs filtered by disc number 1" in {
    getJson(Uri.unsafeParse(s"data/disc/1/${encodedAlbumPath(disc1Song)}")).map { json =>
      val songs = json.as[JsArray]
      songs.value should have size 1
      verifySong(songs(0), "Disc 1 Song", "DiscArtist", "DiscAlbum", 1, 2021)
    }
  }

  "disc returns songs filtered by disc number 2" in {
    getJson(Uri.unsafeParse(s"data/disc/2/${encodedAlbumPath(disc2Song)}")).map { json =>
      val songs = json.as[JsArray]
      songs.value should have size 1
      verifySong(songs(0), "Disc 2 Song", "DiscArtist", "DiscAlbum", 2, 2021)
    }
  }

  "song returns the requested song" in {
    getJson(Uri.unsafeParse(s"data/song/${encodedPath(mp3Song1)}")).map(
      verifySong(_, "Song One", "TestArtist", "TestAlbum", 1, 2020),
    )
  }

  "nextSong returns the following track" in {
    getJson(Uri.unsafeParse(s"data/nextSong/${encodedPath(mp3Song1)}")).map(
      verifySong(_, "Song Two", "TestArtist", "TestAlbum", 2, 2020),
    )
  }
}
