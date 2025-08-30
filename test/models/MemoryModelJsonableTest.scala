package models

import backend.module.TestModuleConfiguration
import com.google.inject.Injector
import common.JsonableSpecs
import common.io.MemoryRoot
import models.ArbitraryModels._
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import org.scalatest.OneInstancePerTest

class MemoryModelJsonableTest extends JsonableSpecs with OneInstancePerTest {
  private val injector: Injector = TestModuleConfiguration().injector
  // TODO check both IO and memory?
  private val mj = injector.instance[ModelJsonable]
  import mj._
  private implicit val root: MemoryRoot = injector.instance[MemoryRoot]
  propJsonTest[Song]()
  propJsonTest[AlbumDir]()
  propJsonTest[ArtistDir]()
}
