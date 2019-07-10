package common

import backend.logging.Logger

trait Debug {
  protected def timed[T](f: => T)(implicit l: Logger): T = timed("Task")(f)
  protected def timed[T](task: String)(f: => T)(implicit l: Logger): T = {
    val start = System.currentTimeMillis
    l.verbose(s"starts $task")
    val $ = f
    l.verbose("%s took %d ms".format(task, System.currentTimeMillis - start))
    $
  }

  protected def trySleep[T](maxTries: Int = 10, sleepTime:Int = 10) (f: => T): T = {
    var numTries = 0
    while (true) {
      try {
        return f
      } catch {
        case _: Exception if numTries < maxTries =>
          numTries += 1
          Thread.sleep(100)
      }
    }
    throw new AssertionError
  }

  protected def echoLocation(implicit l: Logger): Unit = {
    val trace = Thread.currentThread.getStackTrace()(3)
    l.verbose(s"${Thread.currentThread.getName}: ${trace.getClassName}@${trace.getLineNumber}")
  }
}
