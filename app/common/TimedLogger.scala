package common

import backend.logging.{Logger, LoggingLevel}
import javax.inject.Inject

class TimedLogger @Inject() (logger: Logger) {
  def apply[T](task: String, logLevel: LoggingLevel = LoggingLevel.Verbose)(f: => T): T = {
    val start = System.currentTimeMillis
    logger.log(s"starts $task", logLevel)
    val $ = f
    logger.log(s"$task took ${System.currentTimeMillis - start} ms", logLevel)
    $
  }
}
