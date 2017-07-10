package backend.logging

import org.joda.time.DateTime

trait FilteringLogger extends Logger {
  private var currentLevel: LoggingLevel = LoggingLevel.Info
  def setCurrentLevel(l: LoggingLevel): this.type = {
    require(l != null)
    currentLevel = l
    this
  }
  abstract override def log(what: String, level: LoggingLevel, when: DateTime): Unit =
    if (level >= currentLevel) super.log(what, level, when)
}
