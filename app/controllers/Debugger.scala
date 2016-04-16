package controllers
import common.Debug
import play.api.mvc.Action
import search.MetadataCacher
import websockets.WebSocketController

/** used for running manual commands from the client side */
class Debugger extends WebSocketController with Debug {
  def forceRefresh() = Action {
    MetadataCacher.indexAll().map(_.toString) :+ {
      routes.Player.update()
      routes.Searcher.update()
      "Finished"
    } subscribe (safePush(_))
    Ok(views.html.refresh())
  }
}
