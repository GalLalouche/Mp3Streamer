package server

import backend.score.{ModelScore, ScoreBasedProbability, ScoreBasedProbabilityFactory}
import com.google.inject.{Module, Provides}
import models.{FakeModelFactory, Song}
import musicfinder.FakeMusicFiles
import net.codingwell.scalaguice.InjectorExtensions._
import net.codingwell.scalaguice.ScalaModule
import sttp.client3.UriContext

import common.Percentage
import common.path.ref.FileRef
import common.test.memory_ref.MemoryRoot

private class IndexTest(serverModule: Module) extends HttpServerSpecs(serverModule) {
  // The real ScoreBasedProbabilityFactory has assertions that fail with very few songs
  // (expects a distribution of scores across all ModelScore values). This test only needs
  // to verify the endpoint works, so we use a trivial implementation.
  override protected def overridingModule: Module = new ScalaModule {
    @Provides private def scoreBasedProbabilityFactory: ScoreBasedProbabilityFactory =
      (_: Seq[FileRef]) =>
        new ScoreBasedProbability {
          override def apply(s: Song): Percentage = Percentage(0.5)
          override def apply(s: ModelScore): Percentage = Percentage(0.5)
        }
  }

  // Indexing requires at least one song; without it, the score-based probability
  // computation divides by zero.
  private val mf = injector.instance[FakeMusicFiles]
  mf.copySong(new FakeModelFactory(injector.instance[MemoryRoot]).song())

  "index" in {
    getString(uri"index/index") shouldEventuallyReturn "Done"
  }
  // TODO test force refresh — currently broken because the in-memory FS corrupts Avro sync markers
}
