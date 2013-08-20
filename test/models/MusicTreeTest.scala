package models

import org.junit.runner.RunWith
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
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