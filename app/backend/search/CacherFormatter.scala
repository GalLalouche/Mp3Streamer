package backend.search

import backend.recent.NewDir
import backend.search.MetadataCacher.IndexUpdate
import common.io.DirectoryRef
import common.json.{JsonWriteable, ToJsonableOps}
import common.rich.RichT._
import controllers.websockets.WebSocketRef.WebSocketRefReader
import javax.inject.Inject
import models.ModelJsonable._
import play.api.libs.json.Json
import rx.lang.scala.{Observable, Observer}
import songs.SongSelectorState

import scala.concurrent.ExecutionContext

import scalaz.Reader

/** Used for updating the cache from the client. */
class CacherFormatter @Inject()(
    ec: ExecutionContext,
    searchState: SearchState,
    songSelectorState: SongSelectorState,
    cacherFactory: MetadataCacherFactory,
    @NewDir newDirObserver: Observer[DirectoryRef]
) extends ToJsonableOps {
  private implicit val iec: ExecutionContext = ec
  private val cacher = cacherFactory.create

  private implicit val writesIndexUpdate: JsonWriteable[IndexUpdate] = u => Json.obj(
    "finished" -> u.currentIndex,
    "total" -> u.totalNumber,
    "currentDir" -> u.dir.name,
  )

  private def toRefreshStatus(o: Observable[IndexUpdate], updateRecent: Boolean): WebSocketRefReader =
    Reader {ws =>
      if (updateRecent)
        o.map(_.dir) foreach newDirObserver.onNext
      o.map(_.jsonify.toString)
          .doOnNext(ws.broadcast)
          .doOnCompleted {
            songSelectorState.update()
            ws.broadcast("Reloading searcher")
            searchState.update() foreach ws.broadcast("Finished").const
          }.subscribe()
    }

  def forceRefresh(): WebSocketRefReader = toRefreshStatus(cacher.indexAll(), updateRecent = false)
  def quickRefresh(): WebSocketRefReader = toRefreshStatus(cacher.quickRefresh(), updateRecent = true)
}
