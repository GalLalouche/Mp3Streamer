package server

import backend.recon.{Artist, ArtistReconStorage, StoredReconResult}
import backend.storage.DbProvider
import com.google.inject.Module
import models.IOSong
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import play.api.libs.json.Json
import sttp.client3.UriContext
import sttp.model.StatusCode

import scala.concurrent.Future

import cats.implicits.catsSyntaxFlatMapOps

import common.test.BeforeAndAfterEachAsync

private class ScoreTest(serverModule: Module)
    extends HttpServerSpecs(serverModule)
    with BeforeAndAfterEachAsync {

  // Uses the real MP3 test resource because ScorerFormatter hardcodes IOSongTagParser.
  private val file = getResourceFile("/models/song.mp3")
  private val song = IOSong.read(file)
  private val artist = Artist(song.artistName)
  // Must use a relative path because Http4sUtils.decodePath strips the leading '/'.
  private val path = new java.io.File(".").getCanonicalFile.toPath.relativize(file.toPath).toString

  private val artists = injector.instance[ArtistReconStorage]
  private val dbProvider = injector.instance[DbProvider]

  import dbProvider.profile.api._

  private def createAndClearScoreTables: Future[Unit] = {
    val sql = DBIO.seq(
      sqlu"""CREATE TABLE IF NOT EXISTS "artist_score" (
        "name" VARCHAR NOT NULL PRIMARY KEY,
        "score" VARCHAR NOT NULL)""",
      sqlu"""CREATE TABLE IF NOT EXISTS "album_score" (
        "artist" VARCHAR NOT NULL,
        "title" VARCHAR NOT NULL,
        "score" VARCHAR NOT NULL,
        PRIMARY KEY ("artist", "title"))""",
      sqlu"""CREATE TABLE IF NOT EXISTS "song_score" (
        "artist" VARCHAR NOT NULL,
        "album" VARCHAR NOT NULL,
        "song" VARCHAR NOT NULL,
        "score" VARCHAR NOT NULL,
        PRIMARY KEY ("artist", "album", "song"))""",
      sqlu"""DELETE FROM "song_score"""",
      sqlu"""DELETE FROM "album_score"""",
      sqlu"""DELETE FROM "artist_score"""",
    )
    dbProvider.db.run(sql)
  }

  override def beforeEach(): Future[_] =
    artists.utils.clearOrCreateTable() >>
      createAndClearScoreTables >>
      artists.store(artist, StoredReconResult.StoredNull)

  "GET returns empty object when no score exists" in {
    getString(uri"score/$path").map(Json.parse) shouldEventuallyReturn Json.obj()
  }

  "PUT song score returns 204 NoContent" in {
    putRaw(uri"score/song/Okay/$path").map(_.code shouldReturn StatusCode.NoContent)
  }

  "PUT then GET returns updated song score" in {
    for {
      putResponse <- putRaw(uri"score/song/Good/$path")
      getResult <- getString(uri"score/$path").map(Json.parse)
    } yield {
      putResponse.code shouldReturn StatusCode.NoContent
      getResult shouldReturn Json.obj(
        "score" -> "Good",
        "source" -> "Song",
        "song" -> "Good",
        "album" -> "Default",
        "artist" -> "Default",
      )
    }
  }

  "PUT album score then GET" in {
    for {
      putResponse <- putRaw(uri"score/album/Amazing/$path")
      getResult <- getString(uri"score/$path").map(Json.parse)
    } yield {
      putResponse.code shouldReturn StatusCode.NoContent
      getResult shouldReturn Json.obj(
        "score" -> "Amazing",
        "source" -> "Album",
        "song" -> "Default",
        "album" -> "Amazing",
        "artist" -> "Default",
      )
    }
  }

  "PUT artist score then GET" in {
    for {
      putResponse <- putRaw(uri"score/artist/Great/$path")
      getResult <- getString(uri"score/$path").map(Json.parse)
    } yield {
      putResponse.code shouldReturn StatusCode.NoContent
      getResult shouldReturn Json.obj(
        "score" -> "Great",
        "source" -> "Artist",
        "song" -> "Default",
        "album" -> "Default",
        "artist" -> "Great",
      )
    }
  }
}
