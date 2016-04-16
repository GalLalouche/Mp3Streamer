package websockets

import common.Logger


/** Sends console messages to the listeners */
class ConsoleSocket extends WebSocketController with Logger {
  override def trace(s: String) = safePush("[Trace] " + s)
  override def debug(s: String) = safePush("[Debug] " + s)
  override def info(s: String) = safePush("[Info] " + s)
  override def warn(s: String, e: Exception = null) = safePush("[Warn] " + s + " exception: " + e.getMessage)
  override def error(s: String, e: Exception) = safePush("[Error] " + s + " exception: " + e.getMessage)
}
