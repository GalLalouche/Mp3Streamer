

package models

import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import akka.actor.{Actor, ActorSystem}
import akka.testkit.TestActorRef
import common.rich.path.RichFile.richFile
import org.specs2.runner.JUnitRunner
import loggers.Logger

/**
  * Add your spec here.
  * You can mock out a whole application including requests, plugins etc.
  * For more information, consult the wiki.
  */
@RunWith(classOf[JUnitRunner])
class LogFileManagerTest extends Specification with TempDirTest {
	implicit val x = ActorSystem("system")

	class LogManager extends TempDir {
		val logger =mock[Logger]
		val $ = TestActorRef(new LogFileManager(tempDir) {
			override def buildLazyActor = TestActorRef(new Actor {
				override def receive = {
					case f: Function0[_] => f()
				}
			})
			override def buildLogger = logger
		})
	}
	"LogManager" >> {
		"should update logger" >> new LogManager {
			$ ! "hello"
			there was one(logger).info(anyString)
		}
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