package backend.logging

import org.joda.time.DateTime

trait StringOutputLogger extends Logger {
  protected def format(what: String, level: LoggingLevel, time: DateTime): String =
    s"[$level] ($time) $what"
  protected def output(what: String): Unit
  override def log(what: String, level: LoggingLevel, time: DateTime) = output(format(what, level, time))
}
