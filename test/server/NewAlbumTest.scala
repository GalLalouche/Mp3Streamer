package server

import java.time.LocalDate

import backend.mb.AlbumType
import backend.new_albums.NewAlbum
import backend.new_albums.filler.storage.{LastFetchTime, NewAlbumStorage, StoredNewAlbum}
import backend.recon.{Artist, ArtistReconStorage, ReconID, ReconIDArbitrary, StoredReconResult}
import com.google.inject.Module
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import play.api.libs.json.{Json, JsString}
import sttp.client3.UriContext
import sttp.model.StatusCode

import scala.concurrent.Future

import cats.implicits.toFunctorOps

import common.rich.func.kats.ToMoreApplyOps.toMoreApplyOps
import common.test.BeforeAndAfterEachAsync

private class NewAlbumTest(serverModule: Module)
    extends HttpServerSpecs(serverModule)
    with BeforeAndAfterEachAsync {

  private val artistReconStorage = injector.instance[ArtistReconStorage]
  private val lastFetchTime = injector.instance[LastFetchTime]
  private val newAlbumStorage = injector.instance[NewAlbumStorage]

  // Cascades via FK to artist_last_album_update → new_album, and to artist_score.
  override def afterEach(): Future[_] = artistReconStorage.utils.clearTable()

  private def storeArtist(artistName: String): Future[Unit] = artistReconStorage.store(
    Artist(artistName),
    // TODO extract Gen.sample.get to a ScalaCommon utility
    StoredReconResult.unignored(ReconIDArbitrary.gen.sample.get),
  )

  private def markArtistIgnored(artistName: String): Future[Unit] =
    lastFetchTime.ignore(Artist(artistName))

  private def markArtistWithFetchTime(artistName: String): Future[Unit] =
    lastFetchTime.resetToEpoch(Artist(artistName)).void

  private def insertNewAlbum(
      reconId: String,
      albumTitle: String,
      artistName: String,
      albumType: AlbumType = AlbumType.Album,
      epochDay: LocalDate = LocalDate.of(2024, 1, 15),
      isRemoved: Boolean = false,
      isIgnored: Boolean = false,
  ): Future[Unit] = {
    val rid = ReconID(reconId)
    newAlbumStorage.store(
      rid,
      StoredNewAlbum(new NewAlbum(albumTitle, epochDay, Artist(artistName), albumType, rid), isRemoved, isIgnored),
    )
  }

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
    putRaw(uri"new_albums/artist/remove/some_artist") codeShouldEventuallyReturn StatusCode.NoContent
  }

  "PUT artist ignore returns 204" in {
    val artistName = "artist to ignore"
    storeArtist(artistName) *>>
      putRaw(uri"new_albums/artist/ignore/$artistName") codeShouldEventuallyReturn StatusCode.NoContent
  }

  "PUT artist unignore returns 204" in {
    val artistName = "artist to unignore"
    storeArtist(artistName) *>>
      markArtistIgnored(artistName) *>>
      putRaw(uri"new_albums/artist/unignore/$artistName") codeShouldEventuallyReturn StatusCode.NoContent
  }

  "PUT album remove returns 204" in {
    val artistName = "album test artist"
    val reconId = ReconIDArbitrary.gen.sample.get.id
    storeArtist(artistName) *>>
      markArtistWithFetchTime(artistName) *>>
      insertNewAlbum(reconId, "some album", artistName) *>>
      putRaw(uri"new_albums/album/remove/$reconId") codeShouldEventuallyReturn StatusCode.NoContent
  }

  "PUT album ignore returns 204" in {
    val artistName = "album ignore test artist"
    val reconId = ReconIDArbitrary.gen.sample.get.id
    storeArtist(artistName) *>>
      markArtistWithFetchTime(artistName) *>>
      insertNewAlbum(reconId, "some album to ignore", artistName) *>>
      putRaw(uri"new_albums/album/ignore/$reconId") codeShouldEventuallyReturn StatusCode.NoContent
  }
}
