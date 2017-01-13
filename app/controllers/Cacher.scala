package controllers

import backend.logging.LoggingLevel
import common.RichJson._
import common.io.DirectoryRef
import common.rich.func.MoreMonadPlus._
import controllers.websockets.WebSocketController
import play.api.libs.json.{JsObject, JsValue}
import play.api.mvc.Action
import rx.lang.scala.Observable
import search.MetadataCacher
import search.MetadataCacher.IndexUpdate

import scala.concurrent.Promise
import scalaz.syntax.ToBindOps

/** used for running manual commands from the client side */
object Cacher extends WebSocketController
    with ToBindOps {
  private def toJson(u: IndexUpdate): JsObject = JsObject(Map[String, JsValue](
    "finished" -> u.currentIndex,
    "total" -> u.totalNumber,
    "currentDir" -> u.dir.name))
  import Utils.config
  private val cacher = MetadataCacher.create
  // TODO move this parent if the need arise, i.e., there's more than 1 use.
  // TODO bah, state! this should be refactored to somehow extract an observable from a blocking queue or something
  // will be fulfilled when a new WebSocket connection is made
  private var connectionPromise: Promise[Unit] = _
  private def toRefreshStatus(o: Observable[IndexUpdate]) = {
    connectionPromise = Promise()
    config.logger.log("Awaiting connection", LoggingLevel.Verbose)
    Observable.from(connectionPromise.future)
        .>>(o)
        .map(toJson).map(_.toString).doOnCompleted {
      config.logger.log("Finished caching", LoggingLevel.Verbose)
      Player.update()
      broadcast("Reloading searcher")
      Searcher.! onComplete { _ =>
        broadcast("Finished")
        Thread.sleep(1000)
        closeConnections()
      }
    } subscribe { e =>
      broadcast(e)
    }
    Ok(views.html.refresh())
  }

  def newDir(d: DirectoryRef) = cacher ! d
  def forceRefresh() = Action {
    toRefreshStatus(cacher.indexAll())
  }
  def quickRefresh() = Action {
    toRefreshStatus(cacher.quickRefresh())
  }
  override protected def onConnection(): Unit = {
    Option(connectionPromise).foreach { p =>
      if (p.isCompleted)
        config.logger.log("Can't complete promise again!", LoggingLevel.Warn)
      else
        p.success(())
    }
  }
}
