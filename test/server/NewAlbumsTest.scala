package server

import java.time.LocalDate

import backend.mb.AlbumType
import backend.new_albums.NewAlbum
import backend.new_albums.filler.ExistingAlbumsModules
import backend.new_albums.filler.storage.StoredNewAlbum
import backend.recon.{Artist, ArtistReconStorage, ReconID, ReconIDArbitrary, StoredReconResult}
import com.google.inject.Module
import models.FakeModelFactory
import musicfinder.FakeMusicFiles
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import play.api.libs.json.{JsArray, Json, JsString}
import sttp.client3.UriContext
import sttp.model.StatusCode

import scala.concurrent.Future

import cats.implicits.toFunctorOps
import common.rich.func.kats.ToMoreApplyOps.toMoreApplyOps

import common.guice.RichModule.richModule
import common.json.RichJson._
import common.storage.Storage
import common.test.BeforeAndAfterEachAsync
import common.test.GenOps.RichGen
import common.test.memory_ref.MemoryRoot

private class NewAlbumsTest(serverModule: Module)
    extends HttpServerSpecs(serverModule)
    with BeforeAndAfterEachAsync {
  protected override def overridingModule: Module =
    FakeScoreModule.module.overrideWith(ExistingAlbumsModules.lazyAlbums)

  private val artistReconStorage = injector.instance[ArtistReconStorage]
  private val storage = injector.instance[Storage[ReconID, StoredNewAlbum]]

  // Create a song for the happy-path artist so ExistingAlbums can find it.
  injector
    .instance[FakeMusicFiles]
    .copySong(
      new FakeModelFactory(injector.instance[MemoryRoot])
        .song(artistName = "artist with albums", albumName = "Existing Album"),
    )

  private def indexed = getString(uri"index/index")

  // Cascades via FK to artist_last_album_update → new_album, and to artist_score.
  override def afterEach(): Future[_] = artistReconStorage.utils.clearTable()

  private def storeArtist(artistName: String): Future[Unit] = artistReconStorage.store(
    Artist(artistName),
    StoredReconResult.unignored(ReconIDArbitrary.gen.getSample()),
  )

  private def markArtistIgnored(artistName: String): Future[Unit] =
    putRaw(uri"new_albums/artist/ignore/$artistName").void

  private def insertNewAlbum(
      reconId: String,
      albumTitle: String,
      artistName: String,
      albumType: AlbumType = AlbumType.Album,
      epochDay: LocalDate = LocalDate.of(2024, 1, 15),
      isRemoved: Boolean = false,
      isIgnored: Boolean = false,
  ): Future[Unit] = storage.store(
    ReconID(reconId),
    StoredNewAlbum(
      new NewAlbum(albumTitle, epochDay, Artist(artistName), albumType, ReconID(reconId)),
      isRemoved,
      isIgnored,
    ),
  )

  "GET albums returns empty array when no data exists" in {
    getJson(uri"new_albums/albums") shouldEventuallyReturn Json.arr()
  }

  "GET albums for unreconciled artist returns 404" in {
    getRaw(uri"new_albums/albums/nonexistent_artist") codeShouldEventuallyReturn StatusCode.NotFound
  }

  "GET albums for ignored artist returns IGNORED" in {
    val artistName = "ignored artist"
    storeArtist(artistName) *>>
      markArtistIgnored(artistName) *>>
      getJson(uri"new_albums/albums/$artistName") shouldEventuallyReturn JsString("IGNORED")
  }

  "PUT artist remove returns 204" in {
    putRaw(
      uri"new_albums/artist/remove/some_artist",
    ) codeShouldEventuallyReturn StatusCode.NoContent
  }

  "PUT artist ignore returns 204" in {
    val artistName = "artist to ignore"
    storeArtist(artistName) *>>
      putRaw(
        uri"new_albums/artist/ignore/$artistName",
      ) codeShouldEventuallyReturn StatusCode.NoContent
  }

  "PUT artist unignore returns 204" in {
    val artistName = "artist to unignore"
    storeArtist(artistName) *>>
      markArtistIgnored(artistName) *>>
      putRaw(
        uri"new_albums/artist/unignore/$artistName",
      ) codeShouldEventuallyReturn StatusCode.NoContent
  }

  "PUT album remove returns 204" in {
    val artistName = "album test artist"
    val reconId = ReconIDArbitrary.gen.getSample().id
    storeArtist(artistName) *>>
      insertNewAlbum(reconId, "some album", artistName) *>>
      putRaw(uri"new_albums/album/remove/$reconId") codeShouldEventuallyReturn StatusCode.NoContent
  }

  "PUT album ignore returns 204" in {
    val artistName = "album ignore test artist"
    val reconId = ReconIDArbitrary.gen.getSample().id
    storeArtist(artistName) *>>
      insertNewAlbum(reconId, "some album to ignore", artistName) *>>
      putRaw(uri"new_albums/album/ignore/$reconId") codeShouldEventuallyReturn StatusCode.NoContent
  }

  "GET albums for artist with albums returns album array" in {
    val artistName = "artist with albums"
    val reconId1 = ReconIDArbitrary.gen.getSample().id
    val reconId2 = ReconIDArbitrary.gen.getSample().id
    // FakeClock is at epoch; album dates at/before epoch pass isReleased.
    // resetToEpoch gives age=0 < 90d, so filler skips the external fetch.
    indexed *>>
      storeArtist(artistName) *>>
      insertNewAlbum(
        reconId1,
        "First Album",
        artistName,
        epochDay = LocalDate.ofEpochDay(0),
      ) *>>
      insertNewAlbum(
        reconId2,
        "Second Album",
        artistName,
        albumType = AlbumType.EP,
        epochDay = LocalDate.ofEpochDay(0),
      ) *>>
      getJson(uri"new_albums/albums/$artistName").map { result =>
        val albums = result.as[JsArray].value
        albums should have size 2
        // Sorted by (albumType ordinal, -date): Album first, then EP.
        val first = albums(0)
        first.str("title") shouldReturn "First Album"
        first.str("date") shouldReturn "1970/01/01"
        first.str("artistName") shouldReturn artistName
        first.str("albumType") shouldReturn "Album"
        first.str("reconID") shouldReturn reconId1
        val second = albums(1)
        second.str("title") shouldReturn "Second Album"
        second.str("date") shouldReturn "1970/01/01"
        second.str("artistName") shouldReturn artistName
        second.str("albumType") shouldReturn "EP"
        second.str("reconID") shouldReturn reconId2
      }
  }
}
