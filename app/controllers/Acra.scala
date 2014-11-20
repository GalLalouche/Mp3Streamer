package controllers

import java.io.File
import org.joda.time.DateTime
import common.rich.path.{ Directory, RichPath }
import common.rich.path.RichFile._
import play.api.mvc.{ Action, Controller }
import org.joda.time.format.DateTimeFormat
import akka.actor.ActorDSL
import models.KillableActors
import common.LazyActor
import common.rich.path.RichFile
import play.api.libs.json.Json
import play.api.libs.json.JsValue
import common.Jsoner._
import common.Jsoner
import play.api.libs.json.JsObject
import models.LogFileManager

object Acra extends Controller {
	private val logManager = ActorDSL.actor(KillableActors.system)(new LogFileManager(Directory(".") / "logs" / "android" /))
	
	def post = Action { request =>
		val json = request.body.asJson.get.as[JsObject]
		val stack = "Stack Trace:\n" + (json \ "STACK_TRACE").as[String] + "\n\nOther information:\n"
		logManager ! stack + Json.prettyPrint(json - "STACK_TRACE") 
		Ok("")
	}
}