package backend.logging

import common.io.DirectoryRef
import org.joda.time.DateTime

/** Logs each item in its own file, including all lower level tier files */
class DirectoryLogger(dir: DirectoryRef) extends StringOutputLogger {
  private def fileNameForLevel(l: LoggingLevel): String = l.toString
  override def log(what: String, level: LoggingLevel, when: DateTime): Unit =
    level.andLower.foreach(l => dir.addFile(fileNameForLevel(l)).appendLine(format(what, level, when)))
  override protected def output(what: String): Unit = throw new AssertionError()
}
