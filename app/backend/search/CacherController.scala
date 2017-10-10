package backend.search

import backend.search.MetadataCacher.IndexUpdate
import common.rich.RichT._
import controllers.websockets.WebSocketController
import controllers.{Player, Recent}
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Action
import rx.lang.scala.Observable

/** Used for running manual commands from the client side. */
object CacherController extends WebSocketController {
  private def toJson(u: IndexUpdate): JsObject = Json.obj(
    "finished" -> u.currentIndex,
    "total" -> u.totalNumber,
    "currentDir" -> u.dir.name)
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
