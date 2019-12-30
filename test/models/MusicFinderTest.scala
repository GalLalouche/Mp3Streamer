package models

import backend.module.FakeMusicFinder
import org.scalatest.{FreeSpec, OneInstancePerTest}

import common.io.MemoryRoot
import common.rich.collections.RichTraversableOnce._
import common.test.AuxSpecs

class MusicFinderTest extends FreeSpec with OneInstancePerTest with AuxSpecs {
  private val root = new MemoryRoot
  private val mf = new FakeMusicFinder(root) {
    protected override def subDirNames = List("a", "b", "c")
    subDirNames.foreach(root.addSubDir)
    override val extensions = Set("mp3", "flac")
  }

  "MusicFinder" - {
    "find nothing" - {
      def verifyIsEmpty() = mf.getSongFiles shouldBe 'empty
      "when subdirs are empty" in {
        root.addSubDir("a").addSubDir("b").addSubDir("c")
        root.addSubDir("b").addSubDir("b").addSubDir("c")
        root.addSubDir("c").addSubDir("b").addSubDir("c")
        verifyIsEmpty()
      }
      "when file is in root" in {
        root.addFile("foo.mp3")
        verifyIsEmpty()
      }
      "when file is in unlisted dir" in {
        root.addSubDir("d").addFile("foo.mp3")
        verifyIsEmpty()
      }
      "when file has wrong extension" in {
        root.addSubDir("b").addFile("foo.mp2")
        verifyIsEmpty()
      }
    }
    "Find song" in {
      val f = root.addSubDir("a").addSubDir("b").addFile("foo.mp3")
      mf.getSongFiles.single shouldReturn f
    }
  }
}
