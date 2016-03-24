package websockets

import loggers.Logger


/** Sends console messages to the listeners */
object ConsoleSocket extends WebSocketController with Logger {
  private def pushOpt(s: String) = Option(out).map(_._2).foreach(_ push s)
  override def trace(s: String) = pushOpt("[Trace] " + s)
  override def debug(s: String) = pushOpt("[Debug] " + s)
  override def info(s: String) = pushOpt("[Info] " + s)
  override def warn(s: String, e: Exception = null) = pushOpt("[Warn] " + s + " exception: " + e.getMessage)
  override def error(s: String, e: Exception) = pushOpt("[Error] " + s + " exception: " + e.getMessage)
}
