package loggers

trait Logger {
    def trace(s: String)
    def debug(s: String)
	def info(s: String)
    def warn(s: String, e: Exception = null)
    def error(s: String, e: Exception)
    def WTF(s: String, e: Exception) = {
    	e.printStackTrace
    	error("WTF?! " + s, e)
    }
}
////
////case object ConsoleLogger extends Logger {
////	override def logln(s: String) = println(s)
////	override def log(s: String) = print(s)
////}
//
//object defaultLogger extends Logger {
//	var sb: StringBuilder = null
//	override def logln(s: String) = {
//		if (sb != null) {
//			play.Logger.info(sb.toString)
//			sb = null
//		}
//		play.Logger.info(s)
//	}
//	override def log(s: String) = {
//		if (sb == null) {
//			sb = new StringBuilder
//			sb.append(s)			
//		} else 
//			sb.append(", " + s)
//	}
//	override def debug(s :String) = play.Logger.debug(s)
//}
//
//object Logger extends Logger {
//	val loggers = List(defaultLogger)
//	override def logln(s: String) = loggers.foreach(_.logln(s))
//	override def log(s: String) = loggers.foreach(_.log(s))
//	override def debug(s: String) = loggers.foreach(_.debug(s)) 
//}