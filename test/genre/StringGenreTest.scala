package genre

import backend.module.TestModuleConfiguration
import backend.recon.Artist
import musicfinder.FakeMusicFilesImpl
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import org.scalatest.BeforeAndAfterAll
import org.scalatest.freespec.AnyFreeSpec

import common.io.MemoryRoot
import common.test.AuxSpecs

class StringGenreTest extends AnyFreeSpec with AuxSpecs with BeforeAndAfterAll {
  private val root = new MemoryRoot
  private val genresWithSubGenres = Vector("a", "b", "c")
  private val flatGenres = Vector("d")
  private val mf = FakeMusicFilesImpl(
    root,
    genresWithSubGenres,
    flatGenres,
  )
  override def beforeAll(): Unit =
    (genresWithSubGenres ++ flatGenres).foreach(root.addSubDir)
  private val $ = TestModuleConfiguration(_mf = mf).injector.instance[StringGenreFinder]

  private val d = root.addSubDir("d")
  private val d2 = d.addSubDir("d2")
  private val c = root.addSubDir("a").addSubDir("b").addSubDir("c")
  "forDir" - {
    "flat genre" in { $.forDir(d2) shouldReturn StringGenre.Flat("d") }
    "nested genre" in { $.forDir(c) shouldReturn StringGenre.Nested("a", "b") }
  }
  "forArtist" - {
    "flat genre" in { $.forArtist(Artist("d2")).get shouldReturn StringGenre.Flat("d") }
    "nested genre" in { $.forArtist(Artist("c")).get shouldReturn StringGenre.Nested("a", "b") }
    "not found" in { $.forArtist(Artist("foobar")) shouldBe empty }
  }
}
