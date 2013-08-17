package common

import play.libs.Time
import java.io.IOException
import loggers.Logger

trait Debug {
	def timed[T](f: => T): T = timed("Task", loggers.CompositeLogger) {
		f
	}
	
	def timed[T](task: String) (f: => T): T = timed(task, loggers.CompositeLogger) {
		f
	}
	
	def timed[T](task: String, logger: Logger = loggers.CompositeLogger)(f: => T): T = {
		val start = System.currentTimeMillis
		val $ = f
		logger.trace("%s took %d ms".format(task, System.currentTimeMillis - start))
		$
	}
	
	
	def trySleep[T](maxTries: Int) (f: => T): T = {
		var numTries = 0
		while (true) {
			try {
				return f
			} catch {
				case _: Exception if (numTries < maxTries) => {
					numTries += 1
					Thread.sleep(1)
				}
			}
		}
		throw new AssertionError
	}
	
	
	def echoLocation = {
		val trace = Thread.currentThread.getStackTrace()(3);
		println(trace.getClassName() + "@" + trace.getLineNumber())
	}
}