package models

import org.junit.runner.RunWith
import common.path.Directory
import org.specs2.runner.JUnitRunner

/**
  * Add your spec here.
  * You can mock out a whole application including requests, plugins etc.
  * For more information, consult the wiki.
  */
@RunWith(classOf[JUnitRunner])
class MusicFinderTest extends TempDirTest {
	class MusicDir(withDirs: List[String] = List("a", "b", "c")) extends TempDir {
		val $: MusicFinder = withDirs("a", "b")
		def withDirs(dirs: String*): MusicFinder = withDirs(dirs.toList)
		def withDirs(dirs: List[String]) = {
			dirs.foreach(tempDir.addSubDir(_))
			new MusicFinder {
				val dir = tempDir
				val subDirs = dirs
				val extensions = List("mp3", "flac")
			}
		}
	}

	val real = new MusicFinder {
		val dir = Directory("D:/Media/Music")
		val subDirs = List("Metal", "Rock", "Classical", "New Age")
		val extensions = List("mp3", "flac")
	}
	"MusicFinder" >> {
		"find nothing" >> {
			"when subdirs are empty" >> new MusicDir {
				tempDir.addSubDir("a").addSubDir("b").addSubDir("c")
				tempDir.addSubDir("b").addSubDir("b").addSubDir("c")
				tempDir.addSubDir("c").addSubDir("b").addSubDir("c")
				$.getSongFilePaths should be empty
			}
			"when file is in root" >> new MusicDir {
				tempDir.addFile("foo.mp3")
				$.getSongFilePaths should be empty

			}
			"when file is in unlisted dir" >> new MusicDir {
				tempDir.addSubDir("d").addFile("foo.mp3")
				$.getSongFilePaths should be empty
			}
			"when file has wrong extension" >> new MusicDir {
				tempDir.addSubDir("b").addFile("foo.mp2")
				$.getSongFilePaths should be empty
			}
		}
		"Find song in" >> new MusicDir {
			withDirs("a")
			tempDir.addSubDir("a").addSubDir("b").addFile("foo.mp3")
			val x =
				$.getSongFilePaths should contain((tempDir / """a/b/foo.mp3""").path)
		}
	}
//	"real test" >> {
//		timed { println(real.getSongs.length) }
//		1 === 1
//	}
}