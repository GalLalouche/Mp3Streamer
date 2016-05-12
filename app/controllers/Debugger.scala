package controllers
import common.Debug
import play.api.mvc.Action
import search.MetadataCacher
import websockets.WebSocketController
import scala.concurrent.ExecutionContext.Implicits.global

/** used for running manual commands from the client side */
object Debugger extends WebSocketController with Debug {
  def forceRefresh() = Action {
    MetadataCacher.indexAll().map(_.toString).doOnCompleted {
      safePush("Reloading searcher")
      Player.update().onComplete {
       e => safePush("Finished")
      }
    } subscribe (safePush(_))
    Ok(views.html.refresh())
  }
}
