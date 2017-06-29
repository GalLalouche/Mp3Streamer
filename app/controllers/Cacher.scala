package controllers

import controllers.websockets.WebSocketController
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Action
import rx.lang.scala.Observable
import search.MetadataCacher
import search.MetadataCacher.IndexUpdate

/** Used for running manual commands from the client side. */
object Cacher extends WebSocketController {
  private def toJson(u: IndexUpdate): JsObject = Json.obj(
    "finished" -> u.currentIndex,
    "total" -> u.totalNumber,
    "currentDir" -> u.dir.name)
  implicit val c = Utils.config
  import search.ModelJsonable._
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

  def forceRefresh() = Action {
    toRefreshStatus(cacher.indexAll())
  }
  def quickRefresh() = Action {
    toRefreshStatus(cacher.quickRefresh())
  }
}
