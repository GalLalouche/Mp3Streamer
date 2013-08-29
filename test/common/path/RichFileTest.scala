package common.path

import java.io.File
import org.junit.runner.RunWith
import org.specs2.matcher.Expectable
import org.specs2.matcher.Matcher
import models.TempDirTest
import org.specs2.runner.JUnitRunner
import Path.richPath
import Path.poorPath
import org.specs2.mutable.After
import common.path.RichFile._
/**
  * Add your spec here.
  * You can mock out a whole application including requests, plugins etc.
  * For more information, consult the wiki.
  */
@RunWith(classOf[JUnitRunner])
class RichFileTest extends TempDirTest { // yeah yeah, it uses TempDirTest which uses Directory

	"Extension" >> {
		"has extension" >> new TempDir {
			val f = tempDir.addFile("foo.bar")
			f.extension === "bar"
		}
		"has no extension" >> new TempDir {
			val f = tempDir.addFile("foobar")
			f.extension === ""
		}
	}
}