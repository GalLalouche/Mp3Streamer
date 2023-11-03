package common

import javax.inject.Inject

import backend.logging.{Logger, LoggingLevel}

class TimedLogger @Inject() (logger: Logger) {
  def apply[T](task: String, logLevel: LoggingLevel = LoggingLevel.Verbose)(f: => T): T = {
    val start = System.currentTimeMillis
    logger.log(s"starts $task", logLevel)
    val $ = f
    logger.log(s"$task took ${System.currentTimeMillis - start} ms", logLevel)
    $
  }
}
