package backend.logging
import java.time.LocalDateTime

trait StringOutputLogger extends Logger {
  protected def format(what: String, level: LoggingLevel, time: LocalDateTime): String =
    s"[$level] ($time) $what"
  protected def output(what: String): Unit
  override def log(what: String, level: LoggingLevel, time: LocalDateTime) =
    output(format(what, level, time))
}
