package common

import com.google.inject.Inject

class TimedLogger @Inject() {
  def apply[T](task: String, logger: String => Unit = scribe.trace(_))(f: => T): T = {
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
}
