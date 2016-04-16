package loggers

trait Logger {
  def trace(s: String)
  def debug(s: String)
  def info(s: String)
  def warn(s: String, e: Exception = null)
  def error(s: String, e: Exception)
  def WTF(s: String, e: Exception) {
    e.printStackTrace
    error("WTF?! " + s, e)
  }
}
