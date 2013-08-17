package loggers

class ConsoleLogger extends Logger {
	override def trace(s: String) = println("[Trace] " + s)
	override def debug(s: String) = println("[Debug] " + s)
	override def info(s: String) = println("[Info] " + s)
	override def warn(s: String, e: Exception) = println("[Warn] " + s)
	override def error(s: String, e: Exception) = println("[Error] " + s)
}