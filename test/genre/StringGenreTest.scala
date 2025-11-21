package genre

import backend.module.{FakeMusicFinder, TestModuleConfiguration}
import backend.recon.Artist
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import org.scalatest.freespec.AnyFreeSpec

import common.io.MemoryRoot
import common.test.AuxSpecs

class StringGenreTest extends AnyFreeSpec with AuxSpecs {
  private val root = new MemoryRoot
  private val mf = new FakeMusicFinder(root) {
    protected override def genresWithSubGenres = Vector("a", "b", "c")
    override def flatGenres = Vector("d")
    (genresWithSubGenres ++ flatGenres).foreach(root.addSubDir)
    override val extensions = Set("mp3", "flac")
  }
  private val $ = TestModuleConfiguration(_mf = mf).injector.instance[StringGenreFinder]

  private val baseDir = root.getDir("a").get
  private val d = root.getDir("d").get
  private val d2 = d.addSubDir("d2")
  private val c = baseDir.addSubDir("b").addSubDir("c")
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
