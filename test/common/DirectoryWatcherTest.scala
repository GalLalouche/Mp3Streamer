package common

import org.junit.runner.RunWith
import models.MusicFinder
import models.TempDirTest
import org.specs2.runner.JUnitRunner
import models.DirectoryWatcher

/**
  * Add your spec here.
  * You can mock out a whole application including requests, plugins etc.
  * For more information, consult the wiki.
  */
@RunWith(classOf[JUnitRunner])
class MusicFinderTest extends TempDirTest {
	class WatchedDir extends TempDir {
		val f = () => {}
	}
	"DirectoryWatcher" >> {
		"handle new creations" >> {
			"new file created" >> new TempDir {
				
			}
		}
	}
}