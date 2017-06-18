package controllers

import backend.logging.LoggingLevel
import common.RichJson._
import common.io.IODirectory
import controllers.websockets.WebSocketController
import play.api.libs.json.{JsObject, JsValue}
import play.api.mvc.Action
import rx.lang.scala.Observable
import search.MetadataCacher
import search.MetadataCacher.IndexUpdate

import scala.concurrent.Future
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
  private def toRefreshStatus(o: Observable[IndexUpdate]) = {
    config.logger.log("Awaiting connection", LoggingLevel.Verbose)
    o.map(toJson).map(_.toString).doOnCompleted {
      config.logger.log("Finished caching", LoggingLevel.Verbose)
      Player.update()
      broadcast("Reloading searcher")
      Searcher.! onComplete {_ =>
        broadcast("Finished")
        Thread.sleep(1000)
        closeConnections()
      }
    } subscribe {e =>
      broadcast(e)
    }
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
