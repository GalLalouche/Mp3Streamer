package backend.scorer

import backend.module.TestModuleConfiguration
import backend.recon.{Album, Artist, ArtistReconStorage, StoredReconResult}
import backend.scorer.storage.{AlbumScoreStorage, ArtistScoreStorage, SongScoreStorage}
import models.{IOSong, Song}
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import org.scalatest.{AsyncFreeSpec, OneInstancePerTest, Succeeded}
import org.scalatest.tags.Slow
import play.api.libs.json.Json

import scala.concurrent.{ExecutionContext, Future}

import scalaz.std.vector.vectorInstance
import scalaz.Scalaz.{ToBindOps, ToTraverseOpsUnapply}
import common.rich.func.BetterFutureInstances._

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
    // FIXME
    "First test always passes for some reason?!" in Future(Succeeded)
    "Empty object on no score" in {
      $.getScore(path) shouldEventuallyReturn Json.obj()
    }
    "based on song" in {
      artists.store(artist, StoredReconResult.NoRecon) >>
          songScores.store(song, ModelScore.Crappy) >>
          $.getScore(path) shouldEventuallyReturn Json.obj(
        "score" -> "Crappy",
        "source" -> "Song",
        "song" -> "Crappy",
        "album" -> "Default",
        "artist" -> "Default",
      )
    }
    "based on album" in {
      artists.store(artist, StoredReconResult.NoRecon) >>
          albumScores.store(album, ModelScore.Meh) >>
          $.getScore(path) shouldEventuallyReturn Json.obj(
        "score" -> "Meh",
        "source" -> "Album",
        "song" -> "Default",
        "album" -> "Meh",
        "artist" -> "Default",
      )
    }
    "based on artist" in {
      artists.store(artist, StoredReconResult.NoRecon) >>
          artistScores.store(artist, ModelScore.Okay) >>
          $.getScore(path) shouldEventuallyReturn Json.obj(
        "score" -> "Okay",
        "source" -> "Artist",
        "song" -> "Default",
        "album" -> "Default",
        "artist" -> "Okay",
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
        "song" -> "Okay",
        "album" -> "Default",
        "artist" -> "Default",
      )
    }
    "for song overrides" in {
      artists.store(artist, StoredReconResult.NoRecon) >>
          songScores.store(song, ModelScore.Meh) >>
          $.updateSongScore(path, "Good") >>
          $.getScore(path) shouldEventuallyReturn Json.obj(
        "score" -> "Good",
        "source" -> "Song",
        "song" -> "Good",
        "album" -> "Default",
        "artist" -> "Default",
      )
    }
    "for album new" in {
      artists.store(artist, StoredReconResult.NoRecon) >>
          $.updateAlbumScore(path, "Amazing") >>
          $.getScore(path) shouldEventuallyReturn Json.obj(
        "score" -> "Amazing",
        "source" -> "Album",
        "song" -> "Default",
        "album" -> "Amazing",
        "artist" -> "Default",
      )
    }
    "for album overrides" in {
      artists.store(artist, StoredReconResult.NoRecon) >>
          albumScores.store(album, ModelScore.Meh) >>
          $.updateAlbumScore(path, "Amazing") >>
          $.getScore(path) shouldEventuallyReturn Json.obj(
        "score" -> "Amazing",
        "source" -> "Album",
        "song" -> "Default",
        "album" -> "Amazing",
        "artist" -> "Default",
      )
    }
    "for artist new" in {
      artists.store(artist, StoredReconResult.NoRecon) >>
          $.updateArtistScore(path, "Great") >>
          $.getScore(path) shouldEventuallyReturn Json.obj(
        "score" -> "Great",
        "source" -> "Artist",
        "song" -> "Default",
        "album" -> "Default",
        "artist" -> "Great",
      )
    }
    "for artist overrides" in {
      artists.store(artist, StoredReconResult.NoRecon) >>
          artistScores.store(artist, ModelScore.Meh) >>
          $.updateArtistScore(path, "Good") >>
          $.getScore(path) shouldEventuallyReturn Json.obj(
        "score" -> "Good",
        "source" -> "Artist",
        "song" -> "Default",
        "album" -> "Default",
        "artist" -> "Good",
      )
    }
  }
}
