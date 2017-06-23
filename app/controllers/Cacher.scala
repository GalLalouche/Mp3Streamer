package controllers

import common.RichJson._
import common.io.IODirectory
import controllers.websockets.WebSocketController
import play.api.libs.json.{JsObject, JsValue}
import play.api.mvc.Action
import rx.lang.scala.Observable
import search.MetadataCacher
import search.MetadataCacher.IndexUpdate

import scala.concurrent.Future

/** Used for running manual commands from the client side. */
object Cacher extends WebSocketController {
  // TODO extract apply.map to apply with varargs
  private def toJson(u: IndexUpdate): JsObject = JsObject(Map[String, JsValue](
    "finished" -> u.currentIndex,
    "total" -> u.totalNumber,
    "currentDir" -> u.dir.name))
  import Utils.config
  private val cacher = MetadataCacher.create
  private def toRefreshStatus(o: Observable[IndexUpdate]) = {
    o.map(toJson).map(_.toString).doOnCompleted {
      Player.update()
      broadcast("Reloading searcher")
      Searcher.! onComplete {_ =>
        broadcast("Finished")
      }
    } subscribe {broadcast(_)}
    Ok(views.html.refresh())
  }

  def newDir(d: IODirectory): Future[Unit] = cacher processDirectory d
  def forceRefresh() = Action {
    toRefreshStatus(cacher.indexAll())
  }
  def quickRefresh() = Action {
    toRefreshStatus(cacher.quickRefresh())
  }
}
