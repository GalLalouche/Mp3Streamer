package controllers

import java.io.File
import org.joda.time.DateTime
import common.path.{ Directory, Path }
import common.path.RichFile._
import play.api.mvc.{ Action, Controller }
import org.joda.time.format.DateTimeFormat
import akka.actor.ActorDSL
import models.KillableActors
import common.LazyActor
import common.path.RichFile
import play.api.libs.json.Json
import play.api.libs.json.JsValue
import common.Jsoner._
import common.Jsoner
import play.api.libs.json.JsObject

//TODO move logic to model
object Acra extends Controller {
	val logger = loggers.CompositeLogger

	val directory = Directory(".") / "logs" / "android" /
	val lazyActor = ActorDSL.actor(KillableActors.system)(new LazyActor(1000))
	
	def getWritingFunction(s: String) = {
		new Function0[Unit] {
			override def equals(o: Any) = s == o
			override def hashCode = s.hashCode
			override def apply = {
				val name = "ACRA for " + DateTimeFormat.forPattern("dd_MM_yy HH_mm_ss").print(System.currentTimeMillis) + ".txt"
				logger.info(name)
				val logFile = directory.addFile(name)
				logger.info("Caught error from android");
				logFile.write(s);
			}
		}
	}
	def post = Action { request =>
		val json = request.body.asJson.get.as[JsObject]
		val stack = "Stack Trace:\n" + (json \ "STACK_TRACE").as[String] + "\n\nOther information:\n"
		lazyActor ! getWritingFunction(stack + Json.prettyPrint(json - "STACK_TRACE"))		
		Ok("")
	}
}