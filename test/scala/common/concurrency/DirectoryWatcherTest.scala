package common.concurrency

import java.io.File

import common.concurrency.DirectoryWatcher._
import common.rich.path.TempDirectory
import org.scalatest.mock.MockitoSugar
import org.scalatest.{FreeSpec, Matchers, OneInstancePerTest}

class DirectoryWatcherTest extends FreeSpec with MockitoSugar with OneInstancePerTest with Matchers {
  private val tempDir = TempDirectory()
  private val probe = new MessageInterceptor[DirectoryEvent]
  DirectoryWatcher(Seq(tempDir))(probe)
  probe expectMessage DirectoryWatcher.Started // ensures watcher is up and running
  "DirectoryWatcher" - {
    "handle new creations" - {
      "new file created" in {
        tempDir.addFile("foo.bar")
        probe expectMessage OtherChange
      }
      "new folder created" in {
        tempDir.addSubDir("foo")
        probe expectMessage DirectoryCreated(tempDir / "foo" /)
      }
      "new folder created in recursive" in {
        val foo = tempDir.addSubDir("foo")
        probe expectMessage DirectoryCreated(tempDir / "foo" /)
        foo.addSubDir("bar")
        probe expectMessage DirectoryCreated(tempDir / "foo" / "bar" /)
      }
    }
    "handle deletes" - {
      "file deleted" in {
        val file = tempDir.addFile("foo.bar")
        file.delete
        probe expectMessage OtherChange
      }
      "directory deleted" in {
        val d = tempDir.addSubDir("foo")
        probe expectMessage DirectoryCreated(tempDir / "foo" /)
        d.deleteAll
        probe expectMessage DirectoryDeleted(tempDir \ "foo")
      }
    }
    "handle renames" - {
      "directory renamed" in {
        val d = tempDir.addSubDir("foo")
        probe expectMessage DirectoryCreated(tempDir / "foo" /)
        d.dir.renameTo(new File(d.parent, "bar"))
        probe expectMessage DirectoryDeleted(tempDir \ "foo")
        probe expectMessage DirectoryCreated(tempDir / "bar" /)
      }
    }
  }
}
