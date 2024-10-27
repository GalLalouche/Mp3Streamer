package backend.scorer.storage

import backend.module.{FakeMusicFinder, TestModuleConfiguration}
import backend.recon.{Album, Artist, ArtistReconStorage, StoredReconResult}
import backend.recon.Reconcilable.SongExtractor
import backend.scorer.{ModelScore, OptionalModelScore}
import backend.scorer.FullArtistScores.{AlbumScore, ArtistScore, SongScore}
import backend.scorer.OptionalModelScore.Default
import cats.implicits.toTraverseOps
import models.{IOSong, Song}
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import org.scalatest.{AsyncFreeSpec, OneInstancePerTest}

import scala.concurrent.ExecutionContext
import scala.language.higherKinds

import common.rich.func.BetterFutureInstances._
import scalaz.Scalaz.ToBindOps

import common.io.MemoryRoot
import common.test.{AsyncAuxSpecs, BeforeAndAfterEachAsync}

class CompositeStorageScorerTest
    extends AsyncFreeSpec
    with AsyncAuxSpecs
    with BeforeAndAfterEachAsync
    with OneInstancePerTest {
  private val musicFinder: FakeMusicFinder = new FakeMusicFinder(new MemoryRoot)
  private val injector = TestModuleConfiguration(_mf = musicFinder).injector
  implicit override def executionContext: ExecutionContext = injector.instance[ExecutionContext]
  private val song: Song = IOSong.read(getResourceFile("/models/song.mp3"))
  // Not using the extension methods here to avoid importing bugs from it.
  private val artist = Artist(song.artistName)
  private val album = Album(song.albumName, song.year, artist)
  private val $ = injector.instance[CompositeStorageScorer]
  "getArtistScores" in {
    val root = musicFinder.baseDir
    val songFile = root
      .addSubDir(musicFinder.genreDirs.head.name)
      .addSubDir("metal")
      .addSubDir(artist.name)
      .addSubDir(s"${album.year} ${album.title}")
      .addFile(s"${song.trackNumber} - ${song.title}.mp3")
    val artists = injector.instance[ArtistReconStorage]
    val artistScores = injector.instance[ArtistScoreStorage]
    val albumScores = injector.instance[AlbumScoreStorage]
    val songScores = injector.instance[TrackScoreStorage]
    Vector(artists, artistScores, albumScores, songScores).traverse(_.utils.clearOrCreateTable()) >>
      artists.store(artist, StoredReconResult.NoRecon) >>
      artistScores.store(artist, ModelScore.Meh) >>
      songScores.store(song.track, ModelScore.Good) >>
      $.getArtistScores(artist) shouldEventuallyReturn
      ArtistScore(
        artist = artist,
        score = OptionalModelScore.Scored(ModelScore.Meh),
        albumScores = Vector(
          AlbumScore(
            album = album,
            score = Default,
            songScores = Vector(
              SongScore(
                file = songFile,
                trackNumber = song.trackNumber,
                score = OptionalModelScore.Scored(ModelScore.Good),
                title = song.title,
              ),
            ),
          ),
        ),
      )
  }
}
