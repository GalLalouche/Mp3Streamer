package server

import backend.recon.{Artist, ArtistReconStorage, ReconID, StoredReconResult}
import backend.storage.DbProvider
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
  private lazy val dbProvider = injector.instance[DbProvider]

  import dbProvider.profile.api._

  private def setupTables(): Future[_] = {
    val createArtistTable = artistReconStorage.utils.createTableIfNotExists()

    val createOtherTables = dbProvider.db.run(DBIO.seq(
      sqlu"""CREATE TABLE IF NOT EXISTS "artist_last_album_update" (
        "name" VARCHAR PRIMARY KEY,
        "timestamp" TIMESTAMP)""",
      sqlu"""CREATE TABLE IF NOT EXISTS "new_album" (
        "recon_id" VARCHAR,
        "album" VARCHAR,
        "type" VARCHAR,
        "epoch_day" DATE,
        "artist" VARCHAR,
        "is_removed" BOOLEAN DEFAULT FALSE,
        "is_ignored" BOOLEAN DEFAULT FALSE)""",
      sqlu"""CREATE TABLE IF NOT EXISTS "artist_score" (
        "name" VARCHAR PRIMARY KEY,
        "score" VARCHAR)""",
    ))

    for {
      _ <- createArtistTable
      _ <- createOtherTables
    } yield ()
  }

  override def beforeEach(): Future[_] = setupTables()

  override def afterEach(): Future[_] = {
    for {
      _ <- dbProvider.db.run(sqlu"""DELETE FROM "new_album"""")
      _ <- dbProvider.db.run(sqlu"""DELETE FROM "artist_last_album_update"""")
      _ <- dbProvider.db.run(sqlu"""DELETE FROM "artist_score"""")
      _ <- artistReconStorage.utils.clearTable()
    } yield ()
  }

  private def storeArtist(artistName: String): Future[Unit] = {
    val artist = Artist(artistName)
    val reconId = ReconID("00000000-0000-0000-0000-000000000001")
    artistReconStorage.store(artist, StoredReconResult.unignored(reconId))
  }

  private def markArtistIgnored(artistName: String): Future[_] = {
    val normalized = artistName.toLowerCase
    dbProvider.db.run(
      sqlu"""INSERT INTO "artist_last_album_update" ("name", "timestamp") VALUES ($normalized, NULL)""",
    )
  }

  private def markArtistWithFetchTime(artistName: String): Future[_] = {
    val normalized = artistName.toLowerCase
    dbProvider.db.run(
      sqlu"""INSERT INTO "artist_last_album_update" ("name", "timestamp")
             VALUES ($normalized, TIMESTAMP '1970-01-01 00:00:00')""",
    )
  }

  private def insertNewAlbum(
      reconId: String,
      albumTitle: String,
      artistName: String,
      albumType: String = "Album",
      epochDay: java.sql.Date = java.sql.Date.valueOf("2024-01-15"),
      isRemoved: Boolean = false,
      isIgnored: Boolean = false,
  ): Future[_] = {
    val normalized = artistName.toLowerCase
    dbProvider.db.run(
      sqlu"""INSERT INTO "new_album" ("recon_id", "album", "type", "epoch_day", "artist", "is_removed", "is_ignored")
             VALUES ($reconId, $albumTitle, $albumType, $epochDay, $normalized, $isRemoved, $isIgnored)""",
    )
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
