package backend.logging

import org.joda.time.DateTime

class CompositeLogger(loggers: Traversable[Logger]) extends Logger {
  def this(firstLogger: Logger, rest: Logger*) = this(firstLogger :: rest.toList)
  override def log(what: String, level: LoggingLevel, when: DateTime): Unit = loggers.foreach(_.log(what, level, when))
}
