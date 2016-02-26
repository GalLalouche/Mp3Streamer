package common

import loggers.Logger

trait Debug {
	protected def timed[T](f: => T): T = timed("Task", loggers.CompositeLogger) {
		f
	}
	
	protected def timed[T](task: String) (f: => T): T = timed(task, loggers.CompositeLogger) {
		f
	}
	
	protected def timed[T](task: String = "Task", logger: Logger = loggers.CompositeLogger)(f: => T): T = {
		val start = System.currentTimeMillis
		logger.trace(s"starts $task")
		val $ = f
		logger.trace("%s took %d ms".format(task, System.currentTimeMillis - start))
		$
	}
	
	
	protected def trySleep[T](maxTries: Int = 10, sleepTime:Int = 10) (f: => T): T = {
		var numTries = 0
		while (true) {
			try {
				return f
			} catch {
				case _: Exception if (numTries < maxTries) => {
					numTries += 1
					Thread.sleep(100)
				}
			}
		}
		throw new AssertionError
	}
	
	
	protected def echoLocation = {
		val trace = Thread.currentThread.getStackTrace()(3);
		println(trace.getClassName() + "@" + trace.getLineNumber())
	}
}