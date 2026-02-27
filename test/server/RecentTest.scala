package server

import java.time.LocalDateTime

import com.google.inject.Module
import models.{AlbumDir, FakeModelFactory}
import musicfinder.FakeMusicFiles
import net.codingwell.scalaguice.InjectorExtensions._
import play.api.libs.json.{JsArray, JsObject, JsValue}
import sttp.client3.UriContext

import common.FakeClock
import common.rich.RichTime.RichClock
import common.test.memory_ref.MemoryRoot

private class RecentTest(serverModule: Module) extends HttpServerSpecs(serverModule) {
  private val factory = new FakeModelFactory(injector.instance[MemoryRoot])
  private val mf = injector.instance[FakeMusicFiles]
  private val clock = injector.instance[FakeClock]

  private def createAlbumWithSong(lastModified: LocalDateTime = clock.getLocalDateTime) = {
    val album = mf.copyAlbum(factory.album(lastModified = lastModified))
    addSong(album)
    album
  }

  private def addSong(d: AlbumDir) =
    mf.copySong(d.dir.name, factory.song(albumName = d.title, artistName = d.artistName))

  private def createDoubleAlbumWithSongs(
      lastModified: LocalDateTime = clock.getLocalDateTime,
  ) = {
    val album = mf.copyAlbum(factory.album(lastModified = lastModified))
    mf.copySong(
      album.dir.name,
      factory.song(
        filePath = "01-track.mp3",
        albumName = album.title,
        artistName = album.artistName,
        discNumber = Some("1"),
      ),
    )
    mf.copySong(
      album.dir.name,
      factory.song(
        filePath = "02-track.mp3",
        albumName = album.title,
        artistName = album.artistName,
        discNumber = Some("2"),
      ),
    )
    album
  }

  private def titles(json: JsValue): Seq[String] =
    json.as[JsArray].value.map(_.as[JsObject].value("title").as[String]).toSeq

  "all routes" in {
    // Advance clock well past epoch so date arithmetic works (100 days in millis).
    clock.advance(8640000000L)
    val oldRegular = createAlbumWithSong()
    clock.advance(1)
    val oldDouble = createDoubleAlbumWithSongs()

    // Advance 50 more days so recentAlbums are clearly within a 30d window.
    clock.advance(4320000000L)
    val newRegular = createAlbumWithSong()
    clock.advance(1)
    val newDouble = createDoubleAlbumWithSongs()

    for {
      allDefault <- getJson(uri"recent/albums")
      all2 <- getJson(uri"recent/albums/2")
      doubleDefault <- getJson(uri"recent/double")
      double1 <- getJson(uri"recent/double/1")
      since30d <- getJson(uri"recent/since/30d")
    } yield assertAll(
      // /recent/albums returns all 4, most-recent-first
      titles(allDefault) shouldReturn Seq(newDouble.title, newRegular.title, oldDouble.title, oldRegular.title),
      // /recent/albums/2 returns 2 most recent
      titles(all2) shouldReturn Seq(newDouble.title, newRegular.title),
      // /recent/double returns only multi-disc albums, most-recent-first
      titles(doubleDefault) shouldReturn Seq(newDouble.title, oldDouble.title),
      // /recent/double/1 returns only the most recent double album
      titles(double1) shouldReturn Seq(newDouble.title),
      // /recent/since/30d returns only albums modified in the last 30 days
      titles(since30d) shouldReturn Seq(newDouble.title, newRegular.title),
    )
  }
}
