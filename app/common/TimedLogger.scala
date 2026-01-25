package common

import com.google.inject.Inject
import scribe.Level
import scribe.mdc.MDC

class TimedLogger @Inject() {
  // TODO the logger should be taken from the parent here.
  def apply[T](task: String, level: Level = Level.Trace)(f: => T): T =
    apply(task, scribe.log(level, MDC.instance, _))(f)
  def apply[T](task: String, logger: String => Unit)(f: => T): T = {
    val start = System.currentTimeMillis
    try {
      logger(s"starts $task")
      val $ = f
      logger(s"$task took ${System.currentTimeMillis - start} ms")
      $
    } catch {
      case e: Throwable =>
        logger(s"$task FAILED after ${System.currentTimeMillis - start} ms")
        throw e
    }
  }

  def async(task: String, level: Level = Level.Trace): CompletionObserver =
    async(task, scribe.log(level, MDC.instance, _))
  def async(task: String, logger: String => Unit): CompletionObserver = {
    val start = System.currentTimeMillis
    logger(s"starts $task")
    CompletionObserver(
      onCompleted = () => logger(s"$task took ${System.currentTimeMillis - start} ms"),
      onError =
        e => logger(s"$task FAILED after ${System.currentTimeMillis - start} ms: ${e.getMessage}"),
    )
  }
}
