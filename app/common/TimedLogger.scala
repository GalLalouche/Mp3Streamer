package common

import javax.inject.Inject

class TimedLogger @Inject() {
  def apply[T](task: String, logger: String => Unit = scribe.trace(_))(f: => T): T = {
    val start = System.currentTimeMillis
    logger(s"starts $task")
    val $ = f
    logger(s"$task took ${System.currentTimeMillis - start} ms")
    $
  }
}
