package backend.logging

import java.time.LocalDateTime

import backend.configs.Configuration
import common.io.DirectoryRef
import common.rich.RichT._
import net.codingwell.scalaguice.InjectorExtensions._

import scala.concurrent.ExecutionContext

/** Logs each item in its own file, including all lower level tier files */
class DirectoryLogger(rootDirectory: DirectoryRef)(implicit c: Configuration) extends Logger {
  private implicit val ec: ExecutionContext = c.injector.instance[ExecutionContext]
  private val dir = rootDirectory addSubDir "logs"
  private val files: Traversable[FileLogger] = LoggingLevel.values.map(l => {
    val $ = new FileLogger(dir.addFile(l.simpleName + ".log")) with FilteringLogger
    $ setCurrentLevel l
    $
  })
  override def log(what: String, level: LoggingLevel, when: LocalDateTime): Unit =
    files.foreach(_.log(what, level, when))
}
