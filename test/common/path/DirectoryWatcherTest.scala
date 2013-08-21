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
/**
  * Add your spec here.
  * You can mock out a whole application including requests, plugins etc.
  * For more information, consult the wiki.
  */
@RunWith(classOf[JUnitRunner])
class DirectoryTest extends TempDirTest { // yeah yeah, it uses TempDirTest which uses Directory

	"Ctor" >> {
		"throw exception" >> {
			"if file does not exist" >> new TempDir {
				{ Directory("C:/__this_should_not_Ever_EXIST_!@#!@#!13123123") } should throwA[IllegalArgumentException]
			}
			"if file isn't a directory" >> new TempDir {
				val f = tempDir.addFile("file");
				{ Directory(f) } should throwA[IllegalArgumentException]
			}
		}
	}
	class DirTest extends TempDir {
		val $ = tempDir
	}
	def exist = (f: File) => f exists

	def aDirectory = new Matcher[File] {
		def apply[F <: File](s: Expectable[F]) = {
			result(s.value.isDirectory,
				s.description + " is a directory",
				s.description + " is not a directory",
				s)
		}
	}

	"Directory" should {
		"add" >> {
			"file" >> new DirTest {
				$.addFile("foo.bar")
				(new File($.dir, "foo.bar")) should exist
			}
			"directory" >> new DirTest {
				$.addSubDir("foobar")
				(new File($.dir, "foobar")) should aDirectory
			}
		}
		"list files" >> {
			"nothing when no files" >> new DirTest {
				$.files should be empty
			}
			"list all files" >> new DirTest {
				$.addFile("foo.bar")
				$.addFile("bar.foo")
				$.files.toSet === Set(new File(tempDir, "foo.bar"), new File(tempDir, "bar.foo"))
			}
			"not list deep files" >> new DirTest {
				$.addSubDir("foo").addFile("bar")
				$.files should be empty
			}
		}
		"list dirs" >> {
			"nothing when no dirs" >> new DirTest {
				$.dirs should be empty
			}
			"list all dirs" >> new DirTest {
				$.addSubDir("foobar")
				$.addSubDir("barfoo")
				$.dirs.toSet === Set(new File(tempDir, "foobar"), new File(tempDir, "barfoo")).map(Directory(_))
			}
			"not list deep dirs" >> new DirTest {
				$.addSubDir("foo").addSubDir("bar")
				$.dirs === List(Directory(new File(tempDir, "foo")))
			}
		}
		"list deep files both deep and shallow" >> new DirTest {
			$.addFile("foo.bar")
			$.addSubDir("foobar").addFile("bar.foo")
			$.deepFiles.toSet === Set(new File(tempDir, "foo.bar"), new File(tempDir / "foobar" /, "bar.foo"))
		}
		"list deep dirs both deep and shallow" >> new DirTest {
			$.addSubDir("foobar").addSubDir("barfoo")
			$.deepDirs.map(_.dir).toSet === Set(new File(tempDir, "foobar"), new File((tempDir / "foobar" / "barfoo"/).path))
		}
		"list deep paths both deep and shallow" >> new DirTest {
			$.addSubDir("foobar").addFile("bar.foo")
			$.deepPaths.map(_.p).toSet === Set(new File(tempDir, "foobar"), new File((tempDir / "foobar" / "bar.foo").path))
		}
		"clear" >> {
			"not delete self" >> new DirTest {
				$.clear
				$.dir should exist
			}
			"delete all" >> {
				class ClearTest extends DirTest with After {
					def after = {
						$.clear
						$.deepPaths should be empty
					}
				}
				"only files" >> new ClearTest {
					$.addFile("foo.bar")
					$.addFile("bar.foo")
				}
				"only dirs" >> new ClearTest {
					$.addSubDir("foobar")
					$.addSubDir("barfoo")
				}
				"recursive" >> new ClearTest {
					$.addFile("foo.bar")
					$.addSubDir("foobar").addFile("bar.foo")
				}
			}
		}
		"delete should delete self" >> new DirTest {
			$.addFile("foo.bar")
			$.addSubDir("foobar").addFile("bar.foo")
			$.deleteAll
			$.dir should not(exist)
		}
		"parent" >> {
			"return all parent dirs" >> new DirTest {
				val c = tempDir.addSubDir("a").addSubDir("b").addSubDir("c");
				c.parent === (tempDir / "a" / "b" /)
				c.parent.parent === (tempDir / "a" /)
				c.parent.parent.parent === (tempDir)
			}
			"return null on root" >> {
				{ Directory("C:/").parent } should throwA[UnsupportedOperationException]
			}
		}
	}

}