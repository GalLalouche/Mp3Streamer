package backend.scorer

import backend.module.TestModuleConfiguration
import backend.recon.{Album, Artist, ArtistReconStorage, StoredReconResult}
import models.{IOSong, Song}
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import org.scalatest.{AsyncFreeSpec, OneInstancePerTest, Succeeded}
import org.scalatest.tags.Slow
import play.api.libs.json.Json

import scala.concurrent.{ExecutionContext, Future}

import scalaz.std.vector.vectorInstance
import scalaz.Scalaz.{ToBindOps, ToTraverseOpsUnapply}
import common.rich.func.BetterFutureInstances._

import common.concurrency.ThreadlessContext
import common.test.{AsyncAuxSpecs, BeforeAndAfterEachAsync}

@Slow
class ScorerFormatterTest extends AsyncFreeSpec with AsyncAuxSpecs
    // Using EachAsync because https://github.com/scala/bug/issues/9304
    with BeforeAndAfterEachAsync with OneInstancePerTest {
  private val injector = TestModuleConfiguration().injector
  override implicit def executionContext: ExecutionContext = injector.instance[ExecutionContext]
  private val song: Song = IOSong.read(getResourceFile("/models/song.mp3"))
  // Not using the extension methods here to avoid importing bugs from it.
  private val artist = Artist(song.artistName)
  private val album = Album(song.albumName, song.year, artist)
  private val $ = injector.instance[ScorerFormatter]
  private val artists = injector.instance[ArtistReconStorage]
  private val artistScores = injector.instance[ArtistScoreStorage]
  private val albumScores = injector.instance[AlbumScoreStorage]
  private val songScores = injector.instance[SongScoreStorage]

  // TODO extract these to a common method, accepting a bunch of tables
  override protected def beforeEach() = {
    Vector(artists, artistScores, albumScores, songScores).traverse(_.utils.clearOrCreateTable())
  }

  private val path = song.file.path
  "getScores" - {
    "based on song" in {
      artists.store(artist, StoredReconResult.NoRecon) >>
          songScores.store(song, ModelScore.Crappy) >>
          Future.successful(Succeeded) >>
          $.getScore(path) shouldEventuallyReturn Json.obj(
        "score" -> "Crappy",
        "source" -> "Song"
      )
    }
    "based on album" in {
      artists.store(artist, StoredReconResult.NoRecon) >>
          albumScores.store(album, ModelScore.Meh) >>
          $.getScore(path) shouldEventuallyReturn Json.obj(
        "score" -> "Meh",
        "source" -> "Album"
      )
    }
    "based on artist" in {
      artists.store(artist, StoredReconResult.NoRecon) >>
          artistScores.store(artist, ModelScore.Okay) >>
          $.getScore(path) shouldEventuallyReturn Json.obj(
        "score" -> "Okay",
        "source" -> "Artist"
      )
    }
    "returns default on no match" in {
      $.getScore(path) shouldEventuallyReturn Json.obj(
        "score" -> "Default",
      )
    }
  }

  "updateScore" - {
    "for song new" in {
      artists.store(artist, StoredReconResult.NoRecon) >>
          $.updateSongScore(path, "Okay") >>
          $.getScore(path) shouldEventuallyReturn Json.obj(
        "score" -> "Okay",
        "source" -> "Song",
      )
    }
    "for song overrides" in {
      artists.store(artist, StoredReconResult.NoRecon) >>
          songScores.store(song, ModelScore.Meh) >>
          $.updateSongScore(path, "Good") >>
          $.getScore(path) shouldEventuallyReturn Json.obj(
        "score" -> "Good",
        "source" -> "Song",
      )
    }
    "for album new" in {
      artists.store(artist, StoredReconResult.NoRecon) >>
          $.updateAlbumScore(path, "Amazing") >>
          $.getScore(path) shouldEventuallyReturn Json.obj(
        "score" -> "Amazing",
        "source" -> "Album",
      )
    }
    "for album overrides" in {
      artists.store(artist, StoredReconResult.NoRecon) >>
          albumScores.store(album, ModelScore.Meh) >>
          $.updateAlbumScore(path, "Amazing") >>
          $.getScore(path) shouldEventuallyReturn Json.obj(
        "score" -> "Amazing",
        "source" -> "Album",
      )
    }
    "for artist new" in {
      artists.store(artist, StoredReconResult.NoRecon) >>
          $.updateArtistScore(path, "Great") >>
          $.getScore(path) shouldEventuallyReturn Json.obj(
        "score" -> "Great",
        "source" -> "Artist",
      )
    }
    "for artist overrides" in {
      artists.store(artist, StoredReconResult.NoRecon) >>
          artistScores.store(artist, ModelScore.Meh) >>
          $.updateArtistScore(path, "Good") >>
          $.getScore(path) shouldEventuallyReturn Json.obj(
        "score" -> "Good",
        "source" -> "Artist",
      )
    }
  }
}
