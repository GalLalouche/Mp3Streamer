package server

import java.time.LocalDate

import backend.mb.AlbumType
import backend.new_albums.NewAlbum
import backend.new_albums.filler.storage.{LastFetchTime, NewAlbumStorage, StoredNewAlbum}
import backend.recon.{Artist, ArtistReconStorage, ReconID, StoredReconResult}
import com.google.inject.Module
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import play.api.libs.json.{Json, JsString}
import sttp.client3.UriContext
import sttp.model.StatusCode

import scala.concurrent.Future

import common.test.BeforeAndAfterEachAsync

private class NewAlbumTest(serverModule: Module)
    extends HttpServerSpecs(serverModule)
    with BeforeAndAfterEachAsync {

  private lazy val artistReconStorage = injector.instance[ArtistReconStorage]
  private lazy val lastFetchTime = injector.instance[LastFetchTime]
  private lazy val newAlbumStorage = injector.instance[NewAlbumStorage]

  override def beforeEach(): Future[_] = Future.unit

  override def afterEach(): Future[_] =
    artistReconStorage.utils.clearTable()

  private def storeArtist(artistName: String): Future[Unit] = {
    val artist = Artist(artistName)
    val reconId = ReconID("00000000-0000-0000-0000-000000000001")
    artistReconStorage.store(artist, StoredReconResult.unignored(reconId))
  }

  private def markArtistIgnored(artistName: String): Future[Unit] = {
    val artist = Artist(artistName)
    lastFetchTime.ignore(artist)
  }

  private def markArtistWithFetchTime(artistName: String): Future[Unit] = {
    val artist = Artist(artistName)
    lastFetchTime.resetToEpoch(artist).map(_ => ())
  }

  private def insertNewAlbum(
      reconId: String,
      albumTitle: String,
      artistName: String,
      albumType: AlbumType = AlbumType.Album,
      epochDay: LocalDate = LocalDate.of(2024, 1, 15),
      isRemoved: Boolean = false,
      isIgnored: Boolean = false,
  ): Future[Unit] = {
    val artist = Artist(artistName)
    val rid = ReconID(reconId)
    val na = new NewAlbum(albumTitle, epochDay, artist, albumType, rid)
    newAlbumStorage.store(rid, StoredNewAlbum(na, isRemoved, isIgnored))
  }

  "GET albums returns empty array when no data exists" in {
    getJson(uri"new_albums/albums") shouldEventuallyReturn Json.arr()
  }

  "GET albums for unreconciled artist returns 404" in {
    getRaw(uri"new_albums/albums/nonexistent_artist").map(_.code shouldReturn StatusCode.NotFound)
  }

  "GET albums for ignored artist returns IGNORED" in {
    val artistName = "ignored artist"
    for {
      _ <- storeArtist(artistName)
      _ <- markArtistIgnored(artistName)
      result <- getJson(uri"new_albums/albums/$artistName")
    } yield result shouldReturn JsString("IGNORED")
  }

  "PUT artist remove returns 204" in {
    putRaw(uri"new_albums/artist/remove/some_artist").map(
      _.code shouldReturn StatusCode.NoContent,
    )
  }

  "PUT artist ignore returns 204" in {
    val artistName = "artist to ignore"
    for {
      _ <- storeArtist(artistName)
      response <- putRaw(uri"new_albums/artist/ignore/$artistName")
    } yield response.code shouldReturn StatusCode.NoContent
  }

  "PUT artist unignore returns 204" in {
    val artistName = "artist to unignore"
    for {
      _ <- storeArtist(artistName)
      _ <- markArtistIgnored(artistName)
      response <- putRaw(uri"new_albums/artist/unignore/$artistName")
    } yield response.code shouldReturn StatusCode.NoContent
  }

  "PUT album remove returns 204" in {
    val artistName = "album test artist"
    val reconId = "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"
    for {
      _ <- storeArtist(artistName)
      _ <- markArtistWithFetchTime(artistName)
      _ <- insertNewAlbum(reconId, "some album", artistName)
      response <- putRaw(uri"new_albums/album/remove/$reconId")
    } yield response.code shouldReturn StatusCode.NoContent
  }

  "PUT album ignore returns 204" in {
    val artistName = "album ignore test artist"
    val reconId = "11111111-2222-3333-4444-555555555555"
    for {
      _ <- storeArtist(artistName)
      _ <- markArtistWithFetchTime(artistName)
      _ <- insertNewAlbum(reconId, "some album to ignore", artistName)
      response <- putRaw(uri"new_albums/album/ignore/$reconId")
    } yield response.code shouldReturn StatusCode.NoContent
  }
}
