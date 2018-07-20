package backend.logging

import common.io.FileRef

import scala.concurrent.ExecutionContext

// could probably extract an async FileRef if needed
class FileLogger(f: FileRef)(implicit ec: ExecutionContext) extends StringOutputLogger {
  require (f != null)
  override protected def output(what: String): Unit = ec.execute(() => f appendLine what)
}
