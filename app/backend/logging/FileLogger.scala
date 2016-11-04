package backend.logging

import common.rich.path.RichFile

class FileLogger(f: RichFile) extends StringOutputLogger {
  require (f != null)
  override protected def output(what: String): Unit = f appendLine what
}
