package backend.lyrics.retrievers

import backend.StorageSetup
import backend.lyrics.Instrumental
import backend.lyrics.LyricsUrl.DefaultEmpty
import backend.module.TestModuleConfiguration
import backend.recon.{Artist, ArtistReconStorage, StoredReconResult}
import cats.implicits.catsSyntaxFlatMapOps
import models.FakeModelFactory
import net.codingwell.scalaguice.InjectorExtensions._
import org.scalatest.AsyncFreeSpec

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
      artistStorage.store(Artist("foo"), StoredReconResult.StoredNull) >>
      artistStorage.store(Artist(nonExistingArtist), StoredReconResult.StoredNull) >>
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
