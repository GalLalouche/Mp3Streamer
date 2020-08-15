package backend.lyrics.retrievers

import backend.StorageSetup
import backend.lyrics.Instrumental
import backend.module.TestModuleConfiguration
import models.FakeModelFactory
import net.codingwell.scalaguice.InjectorExtensions._
import org.scalatest.AsyncFreeSpec

import scalaz.syntax.bind.ToBindOps
import common.rich.func.BetterFutureInstances._

class InstrumentalArtistTest extends AsyncFreeSpec with StorageSetup {
  override protected val config = TestModuleConfiguration()
  override protected lazy val storage = config.injector.instance[InstrumentalArtistStorage]
  private val factory = new FakeModelFactory
  private val $ = config.injector.instance[InstrumentalArtist]

  "exists" in {
    val song = factory.song(artistName = "foo")
    storage.store("foo")
        .>>($(song)) shouldEventuallyReturn RetrievedLyricsResult.RetrievedLyrics(Instrumental("Default for artist"))
  }
  "doesn't exist" in {
    val song = factory.song(artistName = "foo")
    storage.store("bar").>>($(song)) shouldEventuallyReturn RetrievedLyricsResult.NoLyrics
  }
}
