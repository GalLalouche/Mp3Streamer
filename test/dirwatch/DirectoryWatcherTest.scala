package dirwatch

import org.specs2.mock.Mockito
import akka.actor.ActorSystem
import models.TempDirTest
import akka.testkit.TestActorRef
import akka.testkit.TestKit
import akka.actor.Actor
import akka.actor.TypedActor
import akka.actor.TypedProps
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import akka.testkit.ImplicitSender
import akka.testkit.TestActor
import akka.testkit.TestProbe
import scala.concurrent.duration._
import java.util.concurrent.TimeUnit
import akka.actor.ActorDSL
/**
  * Add your spec here.
  * You can mock out a whole application including requests, plugins etc.
  * For more information, consult the wiki.
  */
@RunWith(classOf[JUnitRunner])
class DirectoryWatcherTest extends TempDirTest with Mockito {
	implicit val x = ActorSystem("test")
	class WatchedDir extends TempDir {
		val folderAdded = mock[() => Unit]
		val other = mock[() => Unit]
		val probe = TestProbe()
		val ref = ActorDSL.actor(new DirectoryWatcher(probe.ref, tempDir))
		Thread.sleep(1000)
	}
	
	import dirwatch.DirectoryWatcher._
	"DirectoryWatcher" >> {
		"handle new creations" >> {
			"new file created" >> new WatchedDir {
				tempDir.addFile("foo.bar")
				probe expectMsg (OtherChange)
			}
			"new folder created" >> new WatchedDir {
				tempDir.addSubDir("foo")
				probe expectMsg (FolderCreated)
			}
			"new folder created in recursive" >> new WatchedDir {
				val foo = tempDir.addSubDir("foo")
				probe expectMsg (FolderCreated)
				foo.addSubDir("bar")
				probe.fishForMessage() {
					case FolderCreated => true
					case _ => false
				}
			}
			//		}
			//		"handle deletes" >> {
			//			"file deleted" >> new WatchedDir {
			//				val file = tempDir.addFile("foo.bar")
			//				file.delete
			//				Thread.sleep(20)
			//				there were two(f).apply
			//			}
			//			"new file created" >> new WatchedDir {
			//				val file = tempDir.addFile("foo.bar")
			//						file.delete
			//						Thread.sleep(20)
			//						there were two(f).apply
			//			}
		}
	}
}