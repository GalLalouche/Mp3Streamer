package backend.logging

import scribe.{Level, Logger}
import scribe.filter.{level, FilterBuilder}
import scribe.format.Formatter
import scribe.output.format.ANSIOutputFormat

import common.rich.primitives.RichOption.richOption

object ScribeUtils {
  def makePretty(): Unit =
    Logger.root
      .clearHandlers()
      .withHandler(formatter = Formatter.enhanced, outputFormat = ANSIOutputFormat)
      .replace()
  def setRootLevel(level: Level): Unit = setLevel(Logger.root, Some(level))
  /** Resets all previous configurations as well. */
  def noLogs(logger: Logger = Logger.root): Unit = {
    logger.reset()
    logger
      .clearHandlers()
      .clearModifiers()
      .withModifier(FilterBuilder().exclude(level <= Level.Fatal))
      .replace()
  }

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
  def setLevel(path: String, level: Level): Unit = setLevel(path, Some(level))
  def setLevel(path: String, level: String): Unit = setLevel(path, parseLevel(level))
  private def setLevel(path: String, l: Option[Level]): Unit =
    setLevel(if (path == "") Logger.root else Logger(path), l)

  private def setLevel(logger: Logger, level: Option[Level]): Unit =
    level.foreach(logger.withMinimumLevel(_).replace())
}
