package backend.logging

trait FilteringLogger extends Logger {
  private var currentLevel: LoggingLevel = LoggingLevel.Info
  def setCurrentLevel(l: LoggingLevel) = {
    require(l != null)
    currentLevel = l
    this
  }
  override def log(what: String, level: LoggingLevel): Unit = if (level >= currentLevel) super.log(what, level)
}
