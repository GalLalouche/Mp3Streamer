package musicfinder

import backend.module.TestModuleConfiguration
import backend.recon.Artist
import models.ArtistDir
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import org.scalatest.FreeSpec
import org.scalatest.Inspectors.forAll

import scala.concurrent.ExecutionContext

import common.io.{IODirectory, JsonableSaver, MemoryRoot}
import common.rich.path.TempDirectory
import common.test.AuxSpecs

class ArtistDirsIndexTest extends FreeSpec with AuxSpecs {
  private val injector = TestModuleConfiguration().injector

  private var dir1: IODirectory = _
  private var dir2: IODirectory = _
  private var dir3: IODirectory = _

  private def setup(saver: JsonableSaver): ArtistDirsIndex = {
    dir1 = IODirectory(TempDirectory())
    dir2 = IODirectory(TempDirectory())
    dir3 = IODirectory(TempDirectory())
    saver
      .saveArray(
        Vector(
          ArtistToDirectory(Artist("foo"), dir1),
          ArtistToDirectory(Artist("bar"), dir1),
          ArtistToDirectory(Artist("moo"), dir2),
        ),
      )
    new ArtistDirsIndex(saver, injector.instance[ExecutionContext])
  }

  "initial value" - {
    lazy val $ = setup(new JsonableSaver(new MemoryRoot))
    "multiples" in {
      $.forDir(dir1) shouldReturn ArtistDirResult.MultipleArtists(
        Vector(Artist("foo"), Artist("bar")),
      )
    }
    "single" in {
      $.forDir(dir2) shouldReturn ArtistDirResult.SingleArtist(Artist("moo"))
    }
    "no match" in {
      $.forDir(dir3) shouldReturn ArtistDirResult.NoMatch
    }
  }

  "updated" - {
    val saver = new JsonableSaver(new MemoryRoot)
    val dir4 = IODirectory(TempDirectory())
    val dir5 = IODirectory(TempDirectory())
    lazy val $ = setup(saver)
    $.update(
      Vector(
        ArtistDir(dir4, "foo2", Set()),
        ArtistDir(dir4, "bar2", Set()),
        ArtistDir(dir5, "moo2", Set()),
      ),
    )
    "multiples" in {
      $.forDir(dir4) shouldReturn ArtistDirResult.MultipleArtists(
        Vector(Artist("foo2"), Artist("bar2")),
      )
    }
    "single" in { $.forDir(dir5) shouldReturn ArtistDirResult.SingleArtist(Artist("moo2")) }
    "no match" in {
      forAll(Vector(dir1, dir2, dir3))($.forDir(_) shouldReturn ArtistDirResult.NoMatch)
    }
  }
}
