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
import common.TestableActor
import akka.actor.ActorRef
import scala.util.Random

class LogFileManager (val dir: Directory) extends Actor {
	import LogFileManager._
	private val logger = loggers.CompositeLogger

	protected def buildLazyActor: ActorRef = context.actorOf(Props(new LazyActor(1000)))
	private lazy val lazyActor = buildLazyActor

	private def getWritingFunction(s: String) = new Function0[Unit] {
		override def equals(o: Any) = s == o
		override def hashCode = s.hashCode
		override def apply = {
			val firstTry = "ACRA for " + DateTimeFormat.forPattern("dd_MM_yy HH_mm_ss").print(System.currentTimeMillis) + ".txt"
			val name = if (dir.files.exists(_.name == firstTry)) firstTry + random.nextLong else firstTry 
				
			val logFile = dir.addFile(name)
			logger.info("Caught error from android");
			logFile.write(s);
		}
	}

	override def receive = {
		case s: String => lazyActor ! getWritingFunction(s)
	}

}

object LogFileManager {
	private val random = new Random
}