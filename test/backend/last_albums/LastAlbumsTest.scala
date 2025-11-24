package backend.last_albums

import backend.module.TestModuleConfiguration
import com.google.inject.Injector
import models.{AlbumDir, FakeModelFactory, FakeModelJsonable}
import net.codingwell.scalaguice.InjectorExtensions._
import org.scalatest.OneInstancePerTest
import org.scalatest.OptionValues._
import org.scalatest.freespec.AnyFreeSpec

import common.FakeClock
import common.io.MemoryRoot
import common.json.saver.JsonableSaver
import common.rich.RichT.richT
import common.rich.RichTime.RichClock
import common.test.AuxSpecs

class LastAlbumsTest extends AnyFreeSpec with OneInstancePerTest with AuxSpecs {
  private val injector: Injector = TestModuleConfiguration().injector
  private val fakeModelFactory = new FakeModelFactory(injector.instance[MemoryRoot])
  private val fakeJsonable = new FakeModelJsonable
  import fakeJsonable._

  private val clock = injector.instance[FakeClock]
  private val saver = injector.instance[JsonableSaver]

  private def create() = {
    clock.advance(1)
    new LastAlbums(clock.getLocalDateTime)
  }
  private def album(): AlbumDir = {
    clock.advance(1)
    fakeModelFactory.album(lastModified = clock.getLocalDateTime)
  }
  private def load(): LastAlbums = {
    clock.advance(1)
    saver.loadObjectOpt[LastAlbums].getOrElse(create())
  }
  def persist(la: LastAlbums): Unit = saver.saveObject(la)

  "load" - {
    "creates empty LastAlbums when no saved data exists" in {
      load().albums shouldBe empty
    }
    "loads previously saved data" in {
      val $ = create()
      val album1 = album()
      val album2 = album()

      $.enqueue(album1).enqueue(album2) |> persist

      load().albums shouldReturn Vector(album1, album2)
    }
  }

  "enqueue" - {
    "adds multiple albums in order" in {
      val $ = create()
      val album1 = album()
      val album2 = album()
      val album3 = album()
      val updated = $.enqueue(album1).enqueue(album2).enqueue(album3)
      updated.albums shouldReturn Vector(album1, album2, album3)
    }
    "does not enqueue album with modification time less than lastUpdateTime" in {
      val $ = create()
      val album1 = album()
      val album2 = album()

      $.enqueue(album2).enqueue(album1).albums shouldReturn Vector(album2)
    }
  }

  "dequeue" - {
    "returns None for empty queue" in {
      create().dequeue shouldReturn None
    }
    "returns first album and updated LastAlbums" in {
      val $ = create()
      val album1 = album()
      val album2 = album()
      val (dequeued, remaining) = $.enqueue(album1).enqueue(album2).dequeue.value
      dequeued shouldReturn album1
      remaining.albums shouldReturn Vector(album2)
    }
    "removes all albums when dequeued multiple times" in {
      val $ = create()
      val album1 = album()
      val album2 = album()
      val updated = $.enqueue(album1).enqueue(album2)

      val (_, remaining2) = updated.dequeue.get._2.dequeue.value
      remaining2.dequeue shouldReturn None
      remaining2.albums shouldBe empty
    }
  }

  "persist" - {
    "saves albums to JsonableSaver" in {
      val $ = create()
      val album1 = album()
      val album2 = album()
      $.enqueue(album1).enqueue(album2) |> persist
      load().albums shouldReturn Vector(album1, album2)
    }
  }
}
