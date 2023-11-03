package backend.lyrics.retrievers

import scalaz.syntax.bind.ToBindOps

import org.scalatest.AsyncFreeSpec

import backend.lyrics.Instrumental
import backend.lyrics.LyricsUrl.DefaultEmpty
import backend.module.TestModuleConfiguration
import backend.recon.{Artist, ArtistReconStorage, StoredReconResult}
import backend.StorageSetup
import common.rich.func.BetterFutureInstances._
import models.FakeModelFactory
import net.codingwell.scalaguice.InjectorExtensions._

class SlickInstrumentalArtistTest extends AsyncFreeSpec with StorageSetup {
  protected override val config = TestModuleConfiguration()
  private val injector = config.injector
  protected override lazy val storage = injector.instance[InstrumentalArtistStorage]
  private val factory = new FakeModelFactory
  private val $ = injector.instance[InstrumentalArtist]

  private val existingArtist = "foo"
  private val nonExistingArtist = "bar"
  override def beforeEach() = {
    val artistStorage = injector.instance[ArtistReconStorage]
    artistStorage.utils.clearOrCreateTable() >>
      artistStorage.store(Artist("foo"), StoredReconResult.NoRecon) >>
      artistStorage.store(Artist(nonExistingArtist), StoredReconResult.NoRecon) >>
      super.beforeEach()
  }

  "exists" in {
    val song = factory.song(artistName = existingArtist)
    storage
      .store(existingArtist)
      .>>($(song)) shouldEventuallyReturn RetrievedLyricsResult.RetrievedLyrics(
      Instrumental("Default for artist", DefaultEmpty),
    )
  }
  "doesn't exist" in {
    val song = factory.song(artistName = existingArtist)
    storage
      .store(nonExistingArtist)
      .>>($(song)) shouldEventuallyReturn RetrievedLyricsResult.NoLyrics
  }
}
