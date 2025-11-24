package musicfinder

import backend.module.TestModuleConfiguration
import backend.recon.Artist
import genre.GenreFinder
import models.ArtistDir
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import org.scalatest.Inspectors.forAll
import org.scalatest.OptionValues._
import org.scalatest.freespec.AnyFreeSpec

import common.io.{DirectoryRef, MemoryRoot, PathRefFactory}
import common.json.saver.{JsonableCOWFactory, JsonableSaver}
import common.test.AuxSpecs

class ArtistDirsIndexTest extends AnyFreeSpec with AuxSpecs {
  private val injector = TestModuleConfiguration().injector
  private implicit val factory: PathRefFactory = injector.instance[PathRefFactory]
  private val root = injector.instance[MemoryRoot]

  private var dir1: DirectoryRef = _
  private var dir2: DirectoryRef = _
  private var dir3: DirectoryRef = _

  private def setup(saver: JsonableSaver): ArtistDirsIndex = {
    dir1 = root.addSubDir("dir1")
    dir2 = root.addSubDir("dir2")
    dir3 = root.addSubDir("dir3")
    saver
      .saveArray(
        Vector(
          ArtistToDirectory(Artist("foo"), dir1),
          ArtistToDirectory(Artist("bar"), dir1),
          ArtistToDirectory(Artist("moo"), dir2),
        ),
      )
    new ArtistDirsIndex(
      new JsonableCOWFactory(saver),
      injector.instance[GenreFinder],
      factory,
    )
  }

  "initial value" - {
    lazy val $ = setup(new JsonableSaver(new MemoryRoot))
    "multiples" in {
      $.forDir(dir1) shouldReturn ArtistDirResult.MultipleArtists(
        Set(Artist("foo"), Artist("bar")),
      )
      $.forArtist(Artist("foo")).value shouldReturn dir1
      $.forArtist(Artist("bar")).value shouldReturn dir1
      $.forArtist(Artist("moo")).value shouldReturn dir2
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
    val dir4 = root.addSubDir("dir4")
    val dir5 = root.addSubDir("dir5")
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
        Set(Artist("foo2"), Artist("bar2")),
      )
    }
    "single" in { $.forDir(dir5) shouldReturn ArtistDirResult.SingleArtist(Artist("moo2")) }
    "no match" in {
      forAll(Vector(dir1, dir2, dir3))($.forDir(_) shouldReturn ArtistDirResult.NoMatch)
    }
  }

  "update with repeats" in {
    val saver = new JsonableSaver(new MemoryRoot)
    val dir1 = root.addSubDir("dir1")
    val dir2 = root.addSubDir("dir2")
    lazy val $ = setup(saver)
    $.update(
      Vector(
        ArtistDir(dir1, "foo", Set()),
        ArtistDir(dir1, "FoO", Set()),
        ArtistDir(dir1, "bAR", Set()),
        ArtistDir(dir2, "Moo", Set()),
      ),
    )
    $.forArtist(Artist("fOo")).value shouldReturn dir1
    $.forArtist(Artist("bar")).value shouldReturn dir1
    $.forArtist(Artist("MOO")).value shouldReturn dir2
  }
}
