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
import java.util.Random
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

	class TempFile extends TempDir {
		val r = new Random
		val $ = RichFile(tempDir.addFile("f" + r.nextInt))
	}
	import resource._
	private def checkClosed(f: File) {
		managed(new PrintStream(f)).acquireAndGet { _.print("foobar2"); }
	}
	"Write" >> {
		"write string" >> new TempFile {
			$.write("foobar");
			managed(new Scanner($)).acquireAndGet { scanner =>
				scanner.nextLine === "foobar";
				scanner.hasNext === false
			}
		}
		"should close" >> new TempFile {
			$.write("barbar")
			checkClosed($)
		}
	}
	"ReadAll" >> {
		"read single line" >> new TempFile {
			for (ps <- managed(new PrintStream($)))
				ps.println("foobar!")
			$.readAll === "foobar!"
		}
		"read multiple lines" >> new TempFile {
			for (ps <- managed(new PrintStream($))) {
				ps.println("foobar!")
				ps.println("foobar2!")
			}
			$.readAll.matches("foobar!\r?\nfoobar2!") === true
		}
		"return an empty string when the file is empty" >> new TempFile {
			$.readAll === ""
		}
		"close" >> new TempFile {
			$.f.exists === true
			$.readAll
			checkClosed($)
		}

	}
}