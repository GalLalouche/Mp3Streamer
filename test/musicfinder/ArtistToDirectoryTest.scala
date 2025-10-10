package musicfinder

import backend.module.TestModuleConfiguration
import models.ArbitraryModels
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import org.scalacheck.Arbitrary

import common.JsonableSpecs
import common.io.{MemoryRoot, PathRefFactory}

private class ArtistToDirectoryTest extends JsonableSpecs {
  private val injector = TestModuleConfiguration().injector
  private implicit val root: MemoryRoot = injector.instance[MemoryRoot]
  private implicit val factory: PathRefFactory = injector.instance[PathRefFactory]
  implicit def arbitraryArtistToDirectory: Arbitrary[ArtistToDirectory] =
    Arbitrary(ArbitraryModels.arbArtist.map(ArtistToDirectory.from))

  propJsonTest[ArtistToDirectory]()
}
