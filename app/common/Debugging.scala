package common

import backend.logging.{Logger, LoggingLevel}

object Debugging {
  def timed[T](task: String, logLevel: LoggingLevel = LoggingLevel.Verbose)(f: => T)(implicit l: Logger): T =
    new TimedLogger(l).apply(task, logLevel)(f)
}