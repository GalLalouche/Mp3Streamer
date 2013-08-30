package models

import common.path.Directory
import akka.actor.ActorDSL
import play.api.mvc.Action
import org.joda.time.format.DateTimeFormat
import common.LazyActor
import akka.actor.Actor
import akka.actor.Props
import common.LazyActor
import common.LazyActor
import common.path.RichFile._

//TODO ad tests
class LogFileManager(val dir: Directory) extends Actor {
	private val logger = loggers.CompositeLogger
	private val lazyActor = context.actorOf(Props(new LazyActor(1000)))

	private def getWritingFunction(s: String) = new Function0[Unit] {
		override def equals(o: Any) = s == o
		override def hashCode = s.hashCode
		override def apply = {
			val name = "ACRA for " + DateTimeFormat.forPattern("dd_MM_yy HH_mm_ss").print(System.currentTimeMillis) + ".txt"
			val logFile = dir.addFile(name)
			logger.info("Caught error from android");
			logFile.write(s);
		}
	}
	
	override def receive = {
		case s: String => lazyActor ! getWritingFunction(s)
	}
	
}