package models

import common.io.IODirectory
import common.rich.path.TempDirectory
import org.scalatest.{FreeSpec, Matchers, OneInstancePerTest}

class MusicFinderTest extends FreeSpec with OneInstancePerTest with Matchers {
  // TODO make work using memory dirs
  private val (tempDir, mf) = {
    val tempDir = TempDirectory()
    val dirs = List("a", "b", "c")
    dirs foreach tempDir.addSubDir
    val mf = new IOMusicFinder {
      override val dir = IODirectory(tempDir)
      protected override val subDirNames = dirs
      override val extensions = Set("mp3", "flac")
    }
    tempDir -> mf
  }

  "MusicFinder" - {
    "find nothing" - {
      def verifyIsEmpty() = mf.getSongFiles shouldBe 'empty
      "when subdirs are empty" in {
        tempDir.addSubDir("a").addSubDir("b").addSubDir("c")
        tempDir.addSubDir("b").addSubDir("b").addSubDir("c")
        tempDir.addSubDir("c").addSubDir("b").addSubDir("c")
        verifyIsEmpty()
      }
      "when file is in root" in {
        tempDir.addFile("foo.mp3")
        verifyIsEmpty()
      }
      "when file is in unlisted dir" in {
        tempDir.addSubDir("d").addFile("foo.mp3")
        verifyIsEmpty()
      }
      "when file has wrong extension" in {
        tempDir.addSubDir("b").addFile("foo.mp2")
        verifyIsEmpty()
      }
    }
    "Find song" in {
      tempDir.addSubDir("a").addSubDir("b").addFile("foo.mp3")
      mf.getSongFiles.map(_.file) should contain((tempDir / "a" / "b" / "foo.mp3").p)
    }
  }
}
