package models

import scala.util.Random
import org.joda.time.format.DateTimeFormat
import akka.actor.{ActorRef, Props}
import akka.actor.Actor
import common.LazyActor
import common.path.Directory
import common.path.RichFile._
import loggers.Logger

class LogFileManager(val dir: Directory) extends Actor {
	import LogFileManager._

	protected def buildLazyActor: ActorRef = context.actorOf(Props(new LazyActor(1000)))
	private lazy val lazyActor = buildLazyActor
	
	protected def buildLogger: Logger = loggers.CompositeLogger
	private lazy val logger = buildLogger

	private def getWritingFunction(s: String) = new Function0[Unit] {
		override def equals(o: Any) = s == o
		override def hashCode = s.hashCode
		override def apply = {
			logger.info("Caught error from android");
			dir.addFile(getFileName).write(s);
		}

		private def getFileName: String = {
			val firstTry = "ACRA for " + DateTimeFormat.forPattern("dd_MM_yy HH_mm_ss").print(System.currentTimeMillis) + ".txt"
			if (dir.files.exists(_.name == firstTry)) firstTry + random.nextLong else firstTry
		}
	}

	override def receive = {
		case s: String => lazyActor ! getWritingFunction(s)
	}

}

object LogFileManager {
	private val random = new Random
}