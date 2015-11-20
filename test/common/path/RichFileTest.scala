//package common.rich.path
//
//import java.io.File
//import org.junit.runner.RunWith
//import org.specs2.matcher.Expectable
//import org.specs2.matcher.Matcher
//import models.TempDirTest
//import org.specs2.runner.JUnitRunner
//import RichPath.richPath
//import RichPath.poorPath
//import org.specs2.mutable.After
//import common.rich.path.RichFile._
//import common.rich.path.RichPath._
//
//import java.util.Scanner
//import java.io.PrintStream
//import java.util.Random
///**
//  * Add your spec here.
//  * You can mock out a whole application including requests, plugins etc.
//  * For more information, consult the wiki.
//  */
//@RunWith(classOf[JUnitRunner])
//class RichFileTest extends TempDirTest { // yeah yeah, it uses TempDirTest which uses Directory
//
//	"Extension" >> {
//		"has extension" >> new TempDir {
//			val f = tempDir.addFile("foo.bar")
//			f.extension === "bar"
//		}
//		"has no extension" >> new TempDir {
//			val f = tempDir.addFile("foobar")
//			f.extension === ""
//		}
//	}
//
//	class TempFile extends TempDir {
//		val r = new Random
//		val $ = RichFile(tempDir.addFile("f" + r.nextInt))
//	}
//import resource._
//	private def checkClosed(f: File) {
//		managed(new PrintStream(f)).acquireAndGet { _.print("foobar2"); }
//	}
//	"Write" >> {
//		"write string" >> new TempFile {
//			$.appendLine("foobar");
//			managed(new Scanner($)).acquireAndGet { scanner =>
//				scanner.nextLine === "foobar";
//				scanner.hasNext === false
//			}
//		}
//		"should close" >> new TempFile {
//			$.appendLine("foo")
//			$.appendLine("bar")
//			managed(new Scanner($)).acquireAndGet { scanner =>
//				scanner.nextLine === "foo";
//				scanner.nextLine === "bar";
//				scanner.hasNext === false
//			}
//		}
//	}
//	"ReadAll" >> {
//		"read single line" >> new TempFile {
//			for (ps <- managed(new PrintStream($)))
//				ps.println("foobar!")
//			$.readAll === "foobar!"
//		}
//		"read multiple lines" >> new TempFile {
//			for (ps <- managed(new PrintStream($))) {
//				ps.println("foobar!")
//				ps.println("foobar2!")
//			}
//			$.readAll.matches("foobar!\r?\nfoobar2!") === true
//		}
//		"return an empty string when the file is empty" >> new TempFile {
//			$.readAll === ""
//		}
//		"close" >> new TempFile {
//			$.f.exists === true
//			$.readAll
//			checkClosed($)
//		}
//	}
//	"Lines" >> new TempFile {
//		val list = List("foobar!", "barfoo?", "nope, definitely foobar")
//		for (ps <- managed(new PrintStream($)))
//			list.foreach(ps.println(_))
//		$.lines.toList === list
//	}
//	"content equals" >> {
//		val bytes1 = Array[Byte](1, 2, 3)
//		val bytes2 = Array[Byte](4, 5, 6)
//		"true for equals" >> {
//			val f1 = new TempFile().$
//			val f2 = new TempFile().$
//			f1.write(bytes1)
//			f2.write(bytes1)
//			f1.hasSameContentAs(f2)
//		}
//		"false for different" >> {
//			val f1 = new TempFile().$
//			val f2 = new TempFile().$
//			f1.write(bytes1)
//			f2.write(bytes2)
//			f1.hasSameContentAs(f2) === false
//		}
//	}
//}