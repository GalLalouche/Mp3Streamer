package models

import backend.module.{FakeMusicFinder, TestModuleConfiguration}
import com.google.inject.Guice
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import org.scalatest.FreeSpec

import common.io.MemoryRoot
import common.test.AuxSpecs

class StringGenreTest extends FreeSpec with AuxSpecs {
  private val root = new MemoryRoot
  private val mf = new FakeMusicFinder(root) {
    protected override def genresWithSubGenres = Vector("a", "b", "c")
    override def flatGenres = Vector("d")
    (genresWithSubGenres ++ flatGenres).foreach(root.addSubDir)
    override val extensions = Set("mp3", "flac")
  }
  private val $ = Guice.createInjector(TestModuleConfiguration(_mf = mf).module).instance[StringGenreFinder]

  // TODO test forArtist
  "forDir" - {
    val a = root.getDir("a").get
    val d = root.getDir("d").get
    "flat genre" in {
      val d2 = d.addSubDir("d2")
      $.forDir(d2) shouldReturn StringGenre.Flat("d")
    }
    "nested genre" in {
      val c = a.addSubDir("b").addSubDir("c")
      $.forDir(c) shouldReturn StringGenre.Nested("a", "b")
    }
  }
}
