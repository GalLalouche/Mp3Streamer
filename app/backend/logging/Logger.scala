package backend.logging

import java.time.LocalDateTime

import common.rich.primitives.RichString

trait Logger {
  def log(what: String, level: LoggingLevel, when: LocalDateTime): Unit
  def log(what: String, level: LoggingLevel): Unit = log(what, level, LocalDateTime.now)
  def verbose(what: String): Unit = log(what, LoggingLevel.Verbose)
  def debug(what: String): Unit = log(what, LoggingLevel.Debug)
  def info(what: String): Unit = log(what, LoggingLevel.Info)
  def warn(what: String): Unit = log(what, LoggingLevel.Warn)
  def warn(what: String, e: Throwable): Unit =
    warn(what + "\n" + RichString.fromPrintStream(e.printStackTrace))
  def error(what: String): Unit = log(what, LoggingLevel.Error)
  def error(what: String, e: Throwable): Unit =
    error(what + "\n" + RichString.fromPrintStream(e.printStackTrace))
}

object Logger {
  object Empty extends Logger {
    override def log(what: String, level: LoggingLevel, when: LocalDateTime): Unit = ()
  }
}
