package backend.lyrics.retrievers

import backend.StorageSetup
import backend.lyrics.Instrumental
import backend.module.TestModuleConfiguration
import models.FakeModelFactory
import net.codingwell.scalaguice.InjectorExtensions._
import org.scalatest.AsyncFreeSpec

import scalaz.std.scalaFuture.futureInstance
import scalaz.syntax.bind.ToBindOps

import common.AuxSpecs

class InstrumentalArtistTest extends AsyncFreeSpec with AuxSpecs with StorageSetup {
  override protected val config = TestModuleConfiguration()
  override protected lazy val storage = config.injector.instance[InstrumentalArtistStorage]
  private val factory = new FakeModelFactory
  private val $ = config.injector.instance[InstrumentalArtist]

  "exists" in {
    val song = factory.song(artistName = "foo")
    storage.store("foo").>>($(song))
        .map(_ shouldReturn RetrievedLyricsResult.RetrievedLyrics(Instrumental("Default for artist")))
  }
  "doesn't exist" in {
    val song = factory.song(artistName = "foo")
    storage.store("bar").>>($(song))
        .map(_ shouldReturn RetrievedLyricsResult.NoLyrics)
  }
}
