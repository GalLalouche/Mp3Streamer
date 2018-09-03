package backend.search

import backend.logging.Logger
import backend.search.MetadataCacher.IndexUpdate
import common.json.{JsonWriteable, ToJsonableOps}
import common.rich.RichT._
import controllers.Recent
import controllers.websockets.WebSocketController
import javax.inject.Inject
import models.ModelJsonable._
import play.api.libs.json.Json
import rx.lang.scala.Observable
import songs.SongSelectorState

import scala.concurrent.ExecutionContext

/** Used for updating the cache from the client. */
class CacherController @Inject()(
    logger: Logger,
    ec: ExecutionContext,
    searchState: SearchState,
    songSelectorState: SongSelectorState,
    cacherFactory: MetadataCacherFactory,
    recent: Recent,
) extends WebSocketController(logger) with ToJsonableOps {
  private implicit val iec: ExecutionContext = ec
  private val cacher = cacherFactory.create

  private implicit val writesIndexUpdate: JsonWriteable[IndexUpdate] = u => Json.obj(
    "finished" -> u.currentIndex,
    "total" -> u.totalNumber,
    "currentDir" -> u.dir.name,
  )

  private def toRefreshStatus(o: Observable[IndexUpdate], updateRecent: Boolean) = {
    if (updateRecent)
      o.map(_.dir) foreach recent.newDir
    o.map(_.jsonify.toString)
        .doOnNext(broadcast)
        .doOnCompleted {
          songSelectorState.!()
          broadcast("Reloading searcher")
          searchState.!() foreach broadcast("Finished").const
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
