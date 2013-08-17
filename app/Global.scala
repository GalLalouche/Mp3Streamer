import models.KillableActors
import play.api.Application
import play.api.GlobalSettings
import play.api.Logger
import websockets.TreeSocket
import scala.actors.Exit

object Global extends GlobalSettings {

	override def onStart(app: Application) {
	}

	override def onStop(app: Application) {
		Logger.info("Application shutdown")
		KillableActors.system.shutdown
	}
}