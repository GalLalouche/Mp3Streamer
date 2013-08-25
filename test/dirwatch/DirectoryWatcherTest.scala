package dirwatch

import java.io.File
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration
import org.junit.runner.RunWith
import org.specs2.mock.Mockito
import org.specs2.runner.JUnitRunner
import org.specs2.time.Duration
import akka.actor.{ActorDSL, ActorSystem}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestProbe}
import akka.util.Timeout._
import akka.util.Timeout
import dirwatch.DirectoryWatcher._
import models.TempDirTest
import java.nio.file.Paths

@RunWith(classOf[JUnitRunner])
class DirectoryWatcherTest extends TempDirTest with Mockito {
	// FFS
	implicit def durationToFinishDuration(d: Duration) = FiniteDuration(d.inMillis, TimeUnit.MILLISECONDS)
	implicit val x = ActorSystem("test")
	class WatchedDir extends TempDir {
		val folderAdded = mock[() => Unit]
		val probe = new TestProbe(x) with DefaultTimeout {
			val x = 10 seconds
			override val timeout: Timeout = x.inMilliseconds
		}
		val ref = ActorDSL.actor(new DirectoryWatcher(probe.ref, tempDir))
		probe expectMsg Started
	}

	"DirectoryWatcher" >> {
		"handle new creations" >> {
			"new file created" >> new WatchedDir {
				tempDir.addFile("foo.bar")
				probe expectMsg OtherChange
			}
			"new folder created" >> new WatchedDir {
				tempDir.addSubDir("foo")
				probe expectMsg DirectoryCreated(tempDir / "foo" /)
			}
			"new folder created in recursive" >> new WatchedDir {
				val foo = tempDir.addSubDir("foo")
				probe expectMsg DirectoryCreated(tempDir / "foo" /)
				foo.addSubDir("bar")
				probe expectMsg DirectoryCreated(tempDir / "foo" / "bar" /)
			}
		}
		"handle deletes" >> {
			"file deleted" >> new WatchedDir {
				val file = tempDir.addFile("foo.bar")
				file.delete
				probe expectMsg (OtherChange)
			}
			"directory deleted" >> new WatchedDir {
				val d = tempDir.addSubDir("foo")
				probe expectMsg DirectoryCreated(tempDir / "foo" /) // waits for change
				d.deleteAll
				probe expectMsg DirectoryDeleted(tempDir \ "foo")
			}
		}
		"handle renames" >> {
			"directory renamed" >> new WatchedDir {
				val d = tempDir.addSubDir("foo")
				probe expectMsg DirectoryCreated(tempDir / "foo" /) // waits for change
				d.dir.renameTo(new File(d.parent, "bar"))
				probe expectMsg DirectoryDeleted(tempDir \ "foo")
				probe expectMsg DirectoryCreated(tempDir / "bar" /)
			}
		}
	}
	"Path" >> {
		"override hashcode and equals" >> new TempDir {
			val p1 = Paths.get(tempDir.path)
			val p2 = Paths.get(tempDir.path)
			p1 should not be p2
			p1.hashCode === p2.hashCode
			p1 === p2
		}
	}
}