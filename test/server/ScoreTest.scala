package server

import backend.recon.{Artist, ArtistReconStorage, StoredReconResult}
import backend.score.storage.TestTableUtils
import com.google.inject.Module
import models.IOSong
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import play.api.libs.json.Json
import sttp.client3.UriContext
import sttp.model.StatusCode

import scala.concurrent.Future

import common.rich.func.kats.ToMoreApplyOps.toMoreApplyOps

import common.test.BeforeAndAfterEachAsync

private class ScoreTest(serverModule: Module)
    extends HttpServerSpecs(serverModule)
    with BeforeAndAfterEachAsync {

  // Uses the real MP3 test resource because ScorerFormatter hardcodes IOSongTagParser.
  private val file = getResourceFile("/models/song.mp3")
  private val artist = Artist(IOSong.read(file).artistName)
  // Must use a relative path because Http4sUtils.decodePath strips the leading '/'.
  private val path = relativePath(file)

  private val artists = injector.instance[ArtistReconStorage]
  private val testTableUtils = injector.instance[TestTableUtils]

  // Children first to respect FK constraints.
  override def beforeEach(): Future[_] =
    testTableUtils.clearAll() *>>
      artists.utils.clearOrCreateTable() *>>
      artists.store(artist, StoredReconResult.StoredNull)

  "GET returns empty object when no score exists" in {
    getString(uri"score/$path").map(Json.parse) shouldEventuallyReturn Json.obj()
  }

  "PUT song score returns 204 NoContent" in {
    putRaw(uri"score/song/Okay/$path") codeShouldEventuallyReturn StatusCode.NoContent
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
