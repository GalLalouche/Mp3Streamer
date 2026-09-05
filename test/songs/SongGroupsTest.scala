package songs

import backend.module.TestModuleConfiguration
import com.google.inject.Injector
import models.FakeModelFactory
import net.codingwell.scalaguice.InjectorExtensions._
import org.scalatest.freespec.AnyFreeSpec

import common.test.AuxSpecs
import common.test.memory_ref.MemoryRoot

class SongGroupsTest extends AnyFreeSpec with AuxSpecs {
  private val injector: Injector = TestModuleConfiguration().injector
  private val fakeModelFactory = new FakeModelFactory(injector.instance[MemoryRoot])

  private val song1 = fakeModelFactory.song()
  private val song2 = fakeModelFactory.song()
  private val song3 = fakeModelFactory.song()
  private val song4 = fakeModelFactory.song()
  private val group1 = SongGroup(Vector(song1, song2))
  private val group2 = SongGroup(Vector(song3, song4))
  private val groups = Vector(group1, group2)
  "fromSongs" in {
    val song5 = fakeModelFactory.song()
    val $ = SongGroups.fromGroups(groups)
    $(song1) shouldReturn group1
    $(song2) shouldReturn group1
    $(song3) shouldReturn group2
    $(song4) shouldReturn group2
    $.get(song5) shouldReturn None
  }
  "save and load" in {
    val $ = injector.instance[SongGroups]
    $.save(groups)
    $.load shouldReturn Set(group1, group2)
  }
}
