package backend.logging

import scribe.{Level, Logger}

import common.rich.primitives.RichOption.richOption

object ScribeUtils {
  def setRootLevel(level: Level): Unit = setLevel(Logger.root, Some(level))
  def noLogs(): Unit = setLevel(Logger.root, None)

  /**
   * Case-insensitive. Returns [[None]] on "off". Throws if input is neither "off" nor one of the
   * built-in levels.
   */
  def parseLevel(level: String): Option[Level] =
    if (level.equalsIgnoreCase("off"))
      None
    else
      Some(Level.get(level).getOrThrow(new IllegalArgumentException(s"Invalid level <$level>")))

  // Use path "" for root.
  def setLevel(path: String, level: String): Unit =
    setLevel(if (path == "") Logger.root else scribe.Logger(path), parseLevel(level))

  private def setLevel(logger: Logger, level: Option[Level]): Unit = {
    logger
      .clearHandlers()
      .clearModifiers()
    level.foreach(l => logger.withHandler(minimumLevel = Some(l)))
    logger.replace()
  }
}
