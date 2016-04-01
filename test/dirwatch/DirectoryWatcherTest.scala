package dirwatch

import java.io.File
import java.util.concurrent.{LinkedBlockingQueue, TimeUnit}

import akka.actor.ActorSystem
import common.rich.path.TempDirectory
import dirwatch.DirectoryWatcher._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{FreeSpec, Matchers, OneInstancePerTest}
import org.specs2.time.Duration
import rx.lang.scala.Subscriber

import scala.concurrent.duration.FiniteDuration

class DirectoryWatcherTest extends FreeSpec with MockitoSugar with OneInstancePerTest with Matchers {
  // FFS
  implicit def durationToFinishDuration(d: Duration) = FiniteDuration(d.inMillis, TimeUnit.MILLISECONDS)
  implicit val x = ActorSystem("test")
  val tempDir = TempDirectory()
  val q = new LinkedBlockingQueue[DirectoryEvent]()
  def expectMessage(msg: DirectoryEvent) {
    q.poll(1, TimeUnit.SECONDS) match {
      case null => fail(s"Expected $msg, but did not receive")
      case e: DirectoryEvent if msg == e => ()
      case _ => expectMessage(msg)
    }
  }
  def acceptMessage(msg: DirectoryEvent) = q offer msg
  DirectoryWatcher(Seq(tempDir)).apply(Subscriber(acceptMessage))
  "DirectoryWatcher" - {
    "handle new creations" - {
      "new file created" in {
        tempDir.addFile("foo.bar")
        expectMessage(OtherChange)
      }
      "new folder created" in {
        tempDir.addSubDir("foo")
        expectMessage(DirectoryCreated(tempDir / "foo" /))
      }
      "new folder created in recursive" in {
        val foo = tempDir.addSubDir("foo")
        expectMessage(DirectoryCreated(tempDir / "foo" /))
        foo.addSubDir("bar")
        expectMessage(DirectoryCreated(tempDir / "foo" / "bar" /))
      }
    }
    "handle deletes" - {
      "file deleted" in {
        val file = tempDir.addFile("foo.bar")
        file.delete
        expectMessage(OtherChange)
      }
      "directory deleted" in {
        val d = tempDir.addSubDir("foo")
        expectMessage(DirectoryCreated(tempDir / "foo" /)) // waits for change
        d.deleteAll
        expectMessage(DirectoryDeleted(tempDir \ "foo"))
      }
    }
    "handle renames" - {
      "directory renamed" in {
        val d = tempDir.addSubDir("foo")
        expectMessage(DirectoryCreated(tempDir / "foo" /)) // waits for change
        d.dir.renameTo(new File(d.parent, "bar"))
        expectMessage(DirectoryDeleted(tempDir \ "foo"))
        expectMessage(DirectoryCreated(tempDir / "bar" /))
      }
    }
  }
}
