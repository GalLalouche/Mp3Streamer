package backend.logging

import scala.concurrent.ExecutionContext

import common.io.FileRef

// could probably extract an async FileRef if needed
class FileLogger(f: FileRef)(implicit ec: ExecutionContext) extends StringOutputLogger {
  require(f != null)
  protected override def output(what: String): Unit = ec.execute(() => f.appendLine(what))
}
