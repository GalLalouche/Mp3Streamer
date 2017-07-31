package backend.logging
import java.time.LocalDateTime

class CompositeLogger(loggers: Traversable[Logger]) extends Logger {
  def this(firstLogger: Logger, rest: Logger*) = this(firstLogger :: rest.toList)
  override def log(what: String, level: LoggingLevel, when: LocalDateTime): Unit =
    loggers.foreach(_.log(what, level, when))
}
