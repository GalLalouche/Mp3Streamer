

package models

import java.io.File
import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import javax.servlet.http.{ HttpServlet, HttpServletRequest, HttpServletResponse }
import play.api.Play
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.test.FakeApplication
import org.specs2.runner.JUnitRunner
import akka.actor._
import akka.testkit.TestProbe
import common.LazyActor
import common.LazyActor
import common.TestableActor
import akka.testkit.TestKit
import akka.testkit.TestKitBase
import akka.testkit.TestActorRef
import common.path.RichFile._

/**
  * Add your spec here.
  * You can mock out a whole application including requests, plugins etc.
  * For more information, consult the wiki.
  */
@RunWith(classOf[JUnitRunner])
class LogFileManagerTest extends Specification with TempDirTest {
	implicit val x = ActorSystem("system")

	class LogManager extends TempDir {
		val $ = TestActorRef(new LogFileManager(tempDir) {
			override def buildLazyActor = TestActorRef(new Actor {
				override def receive = {
					case f: Function0[_] => f()
				}
			})
		})
	}
	"LogManager" >> {
		"create a new file" >> new LogManager {
			$ ! "Hello!"
			tempDir.files.size === 1
		}
		"write text to file" >> new LogManager {
			$ ! "Hello!"
			val f = tempDir.files(0)
			f.readAll === "Hello!"
		}
		"should handle two writes at the same time" >> new LogManager {
			$ ! "Hello"
			$ ! "Goodbye"
			tempDir.files.size === 2
		}
	}
}