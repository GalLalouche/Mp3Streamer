

package models

import java.io.File
import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import play.api.Play
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.test.FakeApplication
import org.specs2.runner.JUnitRunner
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
/**
  * Add your spec here.
  * You can mock out a whole application including requests, plugins etc.
  * For more information, consult the wiki.
  */
@RunWith(classOf[JUnitRunner])
class SongTest extends Specification {
	private def getSong(location: String) = {
		new File(Play.application(FakeApplication()).resource(location).get.getFile.replaceAll("%20", " "))
	}
	val song = getSong("./resources/songs/song.mp3")
	val $ = Song(song)

	"Song" >> {
		"parse id3tag" >> {
			$.title === "Hidden Track"
			$.artist === "Sentenced"
			$.album === "Crimson"
			$.track === 12
			$.year === 2000
			$.bitrate === "192"
			$.duration === 3
			$.size === 75522L
		}

		"jsonify properly" >> {
			val expected = Json obj (
				"size" -> 75522,
				"duration" -> 3,
				"bitrate" -> "192",
				"year" -> 2000,
				"track" -> 12,
				"album" -> "Crimson",
				"artist" -> "Sentenced",
				"title" -> "Hidden Track"
			)
			$.jsonify === expected
		}

		"parse year correctly" >> {
			Song(getSong("./resources/songs/songWithYear.mp3")).year  === 1999
		}
	}

}

class x extends HttpServlet {
	override def doPost(req: HttpServletRequest, resp: HttpServletResponse) {
		req.getParameter("content")
	}
}