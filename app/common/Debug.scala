package common

import backend.logging.{Logger, LoggingLevel}

trait Debug {
  protected def timed[T](task: String, logLevel: LoggingLevel = LoggingLevel.Verbose)(f: => T)(implicit l: Logger): T = {
    val start = System.currentTimeMillis
    l.log(s"starts $task", logLevel)
    val $ = f
    l.log(s"$task took ${System.currentTimeMillis - start} ms", logLevel)
    $
  }
}
