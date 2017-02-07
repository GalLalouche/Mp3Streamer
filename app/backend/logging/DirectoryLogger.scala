package backend.logging

import backend.configs.Configuration
import common.rich.RichT._
import org.joda.time.DateTime

/** Logs each item in its own file, including all lower level tier files */
class DirectoryLogger(implicit val c: Configuration) extends Logger {
  private val dir = c.rootDirectory addSubDir "logs"
  private val files: Traversable[FileLogger] = LoggingLevel.values.map(l => {
    val $ = new FileLogger(dir.addFile(l.simpleName + ".log")) with FilteringLogger
    $ setCurrentLevel l
    $
  })
  override def log(what: String, level: LoggingLevel, when: DateTime): Unit =
    files.foreach(_.log(what, level, when))
}
