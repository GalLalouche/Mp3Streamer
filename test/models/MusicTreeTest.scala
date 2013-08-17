package models

import org.specs2.mutable._
import org.specs2.specification.BeforeAfterExample
import org.apache.commons.io.FileDeleteStrategy
import scala.reflect.io.Path.string2path
import org.junit.runner.RunWith
import java.io.File
import common.path.Directory
import org.scalatest.FlatSpec
import org.scalatest.BeforeAndAfter
import org.scalatest.matchers.ShouldMatchers
import common.Debug
import scala.reflect.io.Path
import play.api.libs.json.Json
import org.specs2.runner.JUnitRunner

/**
  * Add your spec here.
  * You can mock out a whole application including requests, plugins etc.
  * For more information, consult the wiki.
  */
@RunWith(classOf[JUnitRunner])
class MusicTreeTest extends MusicFinderTest {

	class MusicDirTree extends MusicDir {
		override val $: MusicTree = MusicTree(withDirs())
	}
	

	"Tree Jsonifyer" >> {
		"handle simplest cases" >> new MusicDirTree {
			val expected = Json obj ("data" -> Json.obj("title" -> tempDir.name, "attr" -> Json.obj("path" -> tempDir.path)))
			MusicTree.jsonify($.getTree) === expected
		}
	}
}