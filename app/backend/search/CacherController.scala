package backend.search

import backend.search.MetadataCacher.IndexUpdate
import common.Jsonable.ToJsonableOps
import common.rich.RichT._
import controllers.websockets.WebSocketController
import controllers.{ControllerUtils, Player, Recent}
import play.api.libs.json.{Json, Writes}
import play.api.mvc.Action
import rx.lang.scala.Observable

/** Used for updating the cache from the client. */
object CacherController extends WebSocketController with ToJsonableOps {
  import ControllerUtils.config
  import models.ModelJsonable._

  private implicit val writesIndexUpdate: Writes[IndexUpdate] = u => {
    Json.obj(
      "finished" -> u.currentIndex,
      "total" -> u.totalNumber,
      "currentDir" -> u.dir.name)
  }

  private val cacher = MetadataCacher.create
  private def toRefreshStatus(o: Observable[IndexUpdate], updateRecent: Boolean) = {
    if (updateRecent)
      o.map(_.dir) foreach Recent.newDir
    o.map(_.jsonify.toString)
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
