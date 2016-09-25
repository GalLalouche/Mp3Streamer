package controllers

import common.Debug
import common.Jsoner.jsValue
import common.io.DirectoryRef
import play.api.libs.json.{JsObject, JsValue}
import play.api.mvc.Action
import rx.lang.scala.Observable
import search.MetadataCacher
import search.MetadataCacher.IndexUpdate
import websockets.WebSocketController

/** used for running manual commands from the client side */
object Cacher extends WebSocketController with Debug {
  private def toJson(u: IndexUpdate): JsObject = JsObject(Map[String, JsValue](
    "finished" -> u.currentIndex,
    "total" -> u.totalNumber,
    "currentDir" -> u.dir.name))
  private implicit val c = PlayConfig
  private val cacher = MetadataCacher.create
  private def toRefreshStatus(o: Observable[IndexUpdate]) = {
    o.map(toJson).map(_.toString).doOnCompleted {
      Player.update()
      safePush("Reloading searcher")
      Searcher.! onComplete { e =>
        safePush("Finished")
      }
    } subscribe (safePush(_))
    Ok(views.html.refresh())
  }

  def newDir(d: DirectoryRef) = cacher ! d
  def forceRefresh() = Action {
    toRefreshStatus(cacher.indexAll())
  }
  def quickRefresh() = Action {
    toRefreshStatus(cacher.quickRefresh())
  }
}
