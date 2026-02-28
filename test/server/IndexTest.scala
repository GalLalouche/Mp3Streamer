package server

import com.google.inject.Module
import models.FakeModelFactory
import musicfinder.FakeMusicFiles
import net.codingwell.scalaguice.InjectorExtensions._
import sttp.client3.UriContext

import common.rich.func.kats.ToMoreApplyOps.toMoreApplyOps
import common.test.memory_ref.MemoryRoot

private class IndexTest(serverModule: Module) extends HttpServerSpecs(serverModule) {
  override protected def overridingModule: Module = FakeScoreModule.module

  // Indexing requires at least one song; without it, the score-based probability
  // computation divides by zero.
  injector.instance[FakeMusicFiles].copySong(new FakeModelFactory(injector.instance[MemoryRoot]).song())

  "index" in {
    getString(uri"index/index") shouldEventuallyReturn "Done"
  }

  "force refresh" in {
    getString(uri"index/index") *>> getString(uri"index/index?force") shouldEventuallyReturn "Done"
  }
}
