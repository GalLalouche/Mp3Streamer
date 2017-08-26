package controllers

import common.rich.RichT._
import controllers.websockets.WebSocketController
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Action
import rx.lang.scala.Observable
import backend.search.{MetadataCacher, SearchController}
import backend.search.MetadataCacher.IndexUpdate

/** Used for running manual commands from the client side. */
object Cacher extends WebSocketController {
  private def toJson(u: IndexUpdate): JsObject = Json.obj(
    "finished" -> u.currentIndex,
    "total" -> u.totalNumber,
    "currentDir" -> u.dir.name)
  implicit val c = Utils.config
  import models.ModelJsonable._
  private val cacher = MetadataCacher.create
  private def toRefreshStatus(o: Observable[IndexUpdate], updateRecent: Boolean) = {
    if (updateRecent)
      o.map(_.dir) foreach Recent.newDir
    o.map(toJson).map(_.toString)
        .doOnNext(broadcast)
        .doOnCompleted {
          Player.update()
          broadcast("Reloading searcher")
          SearchController.!() foreach broadcast("Finished").const
        }.subscribe()
    Ok(views.html.refresh())
  }

  def forceRefresh() = Action {
    toRefreshStatus(cacher.indexAll(), updateRecent = false)
  }
  def quickRefresh() = Action {
    toRefreshStatus(cacher.quickRefresh(), updateRecent = true)
  }
}
