package backend.logging

import java.time.LocalDateTime

import common.io.DirectoryRef
import common.rich.RichT._

import scala.concurrent.ExecutionContext

/** Logs each item in its own file, including all lower level tier files. */
class DirectoryLogger(rootDirectory: DirectoryRef)(implicit ec: ExecutionContext) extends Logger {
  private val dir = rootDirectory addSubDir "logs"
  private val files: Traversable[FileLogger] = LoggingLevel.values.map(l => {
    val $ = new FileLogger(dir.addFile(l.simpleName + ".log")) with FilteringLogger
    $ setCurrentLevel l
    $
  })
  override def log(what: String, level: LoggingLevel, when: LocalDateTime): Unit =
    files.foreach(_.log(what, level, when))
}
