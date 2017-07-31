package backend.logging
import java.time.LocalDateTime

trait Logger {
  def log(what: String, level: LoggingLevel, when: LocalDateTime): Unit
  def log(what: String, level: LoggingLevel): Unit = log(what, level, LocalDateTime.now)
  def verbose(what: String): Unit = log(what, LoggingLevel.Verbose)
  def debug(what: String): Unit = log(what, LoggingLevel.Debug)
  def info(what: String): Unit = log(what, LoggingLevel.Info)
  def warn(what: String): Unit = log(what, LoggingLevel.Warn)
  def error(what: String): Unit = log(what, LoggingLevel.Error)
}
