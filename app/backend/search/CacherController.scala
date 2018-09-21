package backend.search

import backend.search.MetadataCacher.IndexUpdate
import common.json.{JsonWriteable, ToJsonableOps}
import common.rich.RichT._
import controllers.RecentController
import controllers.websockets.WebSocketRegistryFactory
import javax.inject.Inject
import models.ModelJsonable._
import play.api.libs.json.Json
import play.api.mvc.{InjectedController, WebSocket}
import rx.lang.scala.Observable
import songs.SongSelectorState

import scala.concurrent.ExecutionContext

/** Used for updating the cache from the client. */
class CacherController @Inject()(
    ec: ExecutionContext,
    searchState: SearchState,
    songSelectorState: SongSelectorState,
    cacherFactory: MetadataCacherFactory,
    recent: RecentController,
    webSocketFactory: WebSocketRegistryFactory,
) extends InjectedController with ToJsonableOps {
  private implicit val iec: ExecutionContext = ec
  private val cacher = cacherFactory.create
  private val webSocket = webSocketFactory("CacherController")

  private implicit val writesIndexUpdate: JsonWriteable[IndexUpdate] = u => Json.obj(
    "finished" -> u.currentIndex,
    "total" -> u.totalNumber,
    "currentDir" -> u.dir.name,
  )

  private def toRefreshStatus(o: Observable[IndexUpdate], updateRecent: Boolean) = {
    if (updateRecent)
      o.map(_.dir) foreach recent.newDir
    o.map(_.jsonify.toString)
        .doOnNext(webSocket.broadcast)
        .doOnCompleted {
          songSelectorState.!()
          webSocket.broadcast("Reloading searcher")
          searchState.!() foreach webSocket.broadcast("Finished").const
        }.subscribe()
    Ok(views.html.refresh())
  }

  def forceRefresh() = Action {
    toRefreshStatus(cacher.indexAll(), updateRecent = false)
  }
  def quickRefresh() = Action {
    toRefreshStatus(cacher.quickRefresh(), updateRecent = true)
  }
  def accept(): WebSocket = webSocket.accept()
}
