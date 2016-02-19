package controllers

import akka.actor.{ ActorDSL, actorRef2Scala }
import common.rich.path.Directory
import models.{ KillableActors, LogFileManager }
import play.api.libs.json.{ JsObject, Json }
import play.api.mvc.{ Action, Controller }

object Acra extends Controller {
	private val logManager = ActorDSL.actor(KillableActors.system)(new LogFileManager(Directory(".") / "logs" / "android" /))

	def post = Action { request =>
		val json = request.body.asJson.get.as[JsObject]
		val stack = "Stack Trace:\n" + (json \ "STACK_TRACE").as[String] + "\n\nOther information:\n"
		logManager ! stack + Json.prettyPrint(json - "STACK_TRACE")
		Ok("")
	}
}