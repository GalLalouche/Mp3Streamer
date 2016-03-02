package loggers

import websockets.ConsoleSocket

object CompositeLogger extends Logger {
	private def aLoggerAdapter(l: play.Logger.ALogger): Logger = {
		new Logger {
			def trace(s: String) = l trace (s)
			def debug(s: String) = l debug (s)
			def info(s: String) = l info (s)
			def warn(s: String, e: Exception) = if (e != null) l warn (s, e) else l warn (s)
			def error(s: String, e: Exception) = l error (s, e)
		}
	}
	private val loggers: Seq[Logger] = List(
		aLoggerAdapter(new play.Logger.ALogger(play.api.Logger("Mp3Streamer"))),
		ConsoleSocket
	)

	override def trace(s: String) = loggers.foreach(_.trace(s))
	override def debug(s: String) = loggers.foreach(_.debug(s))
	override def info(s: String) = loggers.foreach(_.info(s))
	override def warn(s: String, e: Exception) = loggers.foreach(_.warn(s, e))
	override def error(s: String, e: Exception) = loggers.foreach(_.error(s, e))
	override def WTF(s: String, e: Exception) = loggers.foreach(_.WTF(s, e))
}