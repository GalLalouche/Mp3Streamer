package controllers

import common.Debug
import common.Jsoner.jsValue
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.{JsObject, JsValue}
import play.api.mvc.Action
import search.{MetadataCacher, RealMetadataCacher}
import search.MetadataCacher.IndexUpdate
import websockets.WebSocketController

/** used for running manual commands from the client side */
object Debugger extends WebSocketController with Debug {
  private def toJson(u: IndexUpdate): JsObject = JsObject(Map[String, JsValue](
    "finished" -> u.currentIndex,
    "total" -> u.totalNumber,
    "currentDir" -> u.dir.name))
  def forceRefresh() = Action {
    RealMetadataCacher.indexAll().map(toJson).map(_.toString).doOnCompleted {
      Player.update()
      safePush("Reloading searcher")
      Searcher.! onComplete {e =>
        safePush("Finished")
      }
    } subscribe (safePush(_))
    Ok(views.html.refresh())
  }
}
