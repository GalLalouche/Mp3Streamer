package server

import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

import com.google.inject.Module
import models.{AlbumDir, FakeModelFactory, ModelJsonable}
import musicfinder.FakeMusicFiles
import net.codingwell.scalaguice.InjectorExtensions._
import sttp.model.Uri

import common.FakeClock
import common.json.ToJsonableOps.parseJsValue
import common.rich.RichT.richT
import common.rich.RichTime.RichClock
import common.test.memory_ref.MemoryRoot

private class RecentTest(serverModule: Module) extends HttpServerSpecs(serverModule) {
  private val factory = new FakeModelFactory(injector.instance[MemoryRoot])
  private val mf = injector.instance[FakeMusicFiles]
  private val clock = injector.instance[FakeClock]

  private def createAlbumWithSong(lastModified: LocalDateTime = clock.getLocalDateTime) =
    mf.copyAlbum(factory.album(lastModified = lastModified)) <| addSong

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

  private val mj = injector.instance[ModelJsonable]
  import mj.albumDirJsonifier

  private def verifyAlbums(path: String, expected: AlbumDir*) =
    getJson(Uri.unsafeParse(s"recent/$path")).map(_.parse[Seq[AlbumDir]] shouldReturn expected.toVector)

  // Advance clock well past epoch so date arithmetic works.
  clock.advance(TimeUnit.DAYS.toMillis(100))
  private val oldRegular = createAlbumWithSong()
  clock.advance(1)
  private val oldDouble = createDoubleAlbumWithSongs()

  clock.advance(TimeUnit.DAYS.toMillis(50))
  private val newRegular = createAlbumWithSong()
  clock.advance(1)
  private val newDouble = createDoubleAlbumWithSongs()

  "albums returns all albums" in
    verifyAlbums("albums", newDouble, newRegular, oldDouble, oldRegular)

  "albums/2 returns only 2 most recent" in
    verifyAlbums("albums/2", newDouble, newRegular)

  "double returns only double albums" in
    verifyAlbums("double", newDouble, oldDouble)

  "double/1 returns only 1 most recent double" in
    verifyAlbums("double/1", newDouble)

  "since/30d returns albums from last 30 days" in
    verifyAlbums("since/30d", newDouble, newRegular)
}
