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
import java.util.Scanner
import java.io.PrintStream
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

	class TempFile extends TempDir with After {
		val $ = RichFile(tempDir.addFile("f"))

		override def after = {
			if ($.exists)
				$.delete === true
		}
	}
	import resource._
	"Write" >> {
		"write string" >> new TempFile {
			$.write("foobar");
			for (scanner <- managed(new Scanner($))) {
				scanner.nextLine === "foobar";
				scanner.hasNext === false
			}
			1 === 1
		}
		"should close" >> new TempFile {
			for (ps <- managed(new PrintStream($))) {
				ps.print("foobar2");
			}
			1 === 1
		}
	}
}