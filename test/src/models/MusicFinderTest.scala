package models

import common.io.IODirectory
import common.rich.path.{Directory, TempDirectory}
import org.scalatest.{FreeSpec, Matchers, OneInstancePerTest}

class MusicFinderTest extends FreeSpec with OneInstancePerTest with Matchers {
  private val (tempDir, mf): (Directory, MusicFinder) = {
    val tempDir = TempDirectory()
    val dirs = List("a", "b", "c")
    dirs foreach tempDir.addSubDir
    val mf = new MusicFinder {
      override val dir = IODirectory(tempDir)
      override val subDirs = dirs
      override val extensions = List("mp3", "flac")
    }
    tempDir -> mf
  }

  "MusicFinder" - {
    "find nothing" - {
      def isEmpty() = mf.getSongFilePaths should be === Seq()
      "when subdirs are empty" in {
        tempDir.addSubDir("a").addSubDir("b").addSubDir("c")
        tempDir.addSubDir("b").addSubDir("b").addSubDir("c")
        tempDir.addSubDir("c").addSubDir("b").addSubDir("c")
        isEmpty()
      }
      "when file is in root" in {
        tempDir.addFile("foo.mp3")
        isEmpty()
      }
      "when file is in unlisted dir" in {
        tempDir.addSubDir("d").addFile("foo.mp3")
        isEmpty()
      }
      "when file has wrong extension" in {
        tempDir.addSubDir("b").addFile("foo.mp2")
        isEmpty()
      }
    }
    "Find song" in {
      tempDir.addSubDir("a").addSubDir("b").addFile("foo.mp3")
      mf.getSongFilePaths should contain((tempDir / """a/b/foo.mp3""").path)
    }
  }
}
