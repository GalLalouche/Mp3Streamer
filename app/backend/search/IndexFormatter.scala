package backend.search

import backend.search.MetadataCacher.CacheUpdate
import controllers.websockets.WebSocketRef.AsyncWebSocketRefReader
import javax.inject.Inject
import play.api.libs.json.Json
import rx.lang.scala.{Observable, Observer}

import scalaz.Reader

import common.json.JsonWriteable
import common.json.ToJsonableOps._
import common.rich.RichObservable._

private class IndexFormatter @Inject()(indexer: Indexer) {
  private implicit val indexUpdateWritable: JsonWriteable[CacheUpdate] = u => Json.obj(
    "finished" -> u.currentIndex,
    "total" -> u.totalNumber,
    "currentDir" -> u.dir.name,
  )

  def cacheAll(): AsyncWebSocketRefReader = broadcastUpdates(indexer.cacheAll())
  def quickRefresh(): AsyncWebSocketRefReader = broadcastUpdates(indexer.quickRefresh())

  private def broadcastUpdates(o: Observable[CacheUpdate]): AsyncWebSocketRefReader = Reader {ws =>
    o
        .map(_.jsonify.toString)
        .subscribeWithNotification(new Observer[String] {
          override def onNext(value: String): Unit = ws.broadcast(value)
          override def onCompleted(): Unit = ws.broadcast("Finished")
        })
  }
}
