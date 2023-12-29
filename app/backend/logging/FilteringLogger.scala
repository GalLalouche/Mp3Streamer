package backend.logging

import java.time.LocalDateTime

trait FilteringLogger extends Logger {
  private var currentLevel: LoggingLevel = LoggingLevel.Info
  def setCurrentLevel(l: LoggingLevel): this.type = {
    require(l != null)
    currentLevel = l
    this
  }
  abstract override def log(what: String, level: LoggingLevel, when: LocalDateTime): Unit =
    if (level >= currentLevel) super.log(what, level, when)
}
