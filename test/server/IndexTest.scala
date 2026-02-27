package server

import com.google.inject.Module
import models.FakeModelFactory
import musicfinder.FakeMusicFiles
import net.codingwell.scalaguice.InjectorExtensions._
import sttp.client3.UriContext

import common.test.memory_ref.MemoryRoot

private class IndexTest(serverModule: Module) extends HttpServerSpecs(serverModule) {
  override protected def overridingModule: Module = FakeScoreModule.module

  // Indexing requires at least one song; without it, the score-based probability
  // computation divides by zero.
  private val mf = injector.instance[FakeMusicFiles]
  mf.copySong(new FakeModelFactory(injector.instance[MemoryRoot]).song())

  "index" in {
    getString(uri"index/index") shouldEventuallyReturn "Done"
  }
  // TODO test force refresh — currently broken because the in-memory FS corrupts Avro sync markers
}
