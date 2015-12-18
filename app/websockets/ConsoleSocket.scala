package websockets

/** Sends console messages to the listeners */
object ConsoleSocket extends WebSocketController with loggers.Logger {
	override def trace(s: String) = out._2.push("[Trace] " + s)
	override def debug(s: String) = out._2.push("[Debug] " + s)
	override def info(s: String) = out._2.push("[Info] " + s)
	override def warn(s: String, e: Exception = null) = out._2.push("[Warn] " + s + " exception: " + e.getMessage)
	override def error(s: String, e: Exception) = out._2.push("[Error] " + s + " exception: " + e.getMessage)
}