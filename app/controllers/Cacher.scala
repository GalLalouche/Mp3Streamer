package controllers

import common.Debug
import common.Jsoner.jsValue
import common.io.DirectoryRef
import controllers.websockets.WebSocketController
import play.api.libs.json.{JsObject, JsValue}
import play.api.mvc.Action
import rx.lang.scala.Observable
import search.MetadataCacher
import search.MetadataCacher.IndexUpdate

import scala.concurrent.Promise
import scalaz.Bind
import scalaz.syntax.ToBindOps

/** used for running manual commands from the client side */
object Cacher extends WebSocketController with Debug
    with ToBindOps {
  //TODO this should probably be moved to common.concurrency or something
  private implicit object ObservableFunctor extends Bind[Observable] {
    override def bind[A, B](fa: Observable[A])(f: A => Observable[B]): Observable[B] = fa flatMap f
    override def map[A, B](fa: Observable[A])(f: A => B): Observable[B] = fa map f
  }
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
    Observable.from(connectionPromise.future)
        .>>(o)
        .map(toJson).map(_.toString).doOnCompleted {
      Player.update()
      safePush("Reloading searcher")
      Searcher.! onComplete { e =>
        safePush("Finished")
      }
    } subscribe { e =>
      safePush(e)
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
    Option(connectionPromise).foreach(_.success(()))
  }
}
