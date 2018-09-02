package backend.search

import backend.search.MetadataCacher.IndexUpdate
import common.json.{JsonWriteable, ToJsonableOps}
import common.rich.RichT._
import controllers.Recent
import controllers.websockets.WebSocketController
import models.ModelJsonable._
import net.codingwell.scalaguice.InjectorExtensions._
import play.api.libs.json.Json
import play.api.mvc.Action
import rx.lang.scala.Observable
import songs.SongSelectorState

import scala.concurrent.ExecutionContext

/** Used for updating the cache from the client. */
object CacherController extends WebSocketController with ToJsonableOps {
  private implicit val ec: ExecutionContext = injector.instance[ExecutionContext]
  private val searchState = injector.instance[SearchState]
  private val songSelectorState = injector.instance[SongSelectorState]

  private implicit val writesIndexUpdate: JsonWriteable[IndexUpdate] = u => Json.obj(
    "finished" -> u.currentIndex,
    "total" -> u.totalNumber,
    "currentDir" -> u.dir.name,
  )

  private val cacher = injector.instance[MetadataCacherFactory].create
  private def toRefreshStatus(o: Observable[IndexUpdate], updateRecent: Boolean) = {
    if (updateRecent)
      o.map(_.dir) foreach Recent.newDir
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
