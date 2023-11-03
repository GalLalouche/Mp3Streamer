package backend.recent

import controllers.websockets.PlayWebSocketRegistryFactory
import controllers.websockets.WebSocketRef.WebSocketRefReader
import javax.inject.Inject
import models.Album
import models.ModelJsonable.AlbumJsonifier
import play.api.libs.json.JsValue
import rx.lang.scala.Observable

import scala.concurrent.{ExecutionContext, Future}

import scalaz.Reader

import common.json.ToJsonableOps._
import common.rich.collections.RichTraversableOnce.richTraversableOnce
import common.rich.RichT._

private class RecentFormatter @Inject() (
    ec: ExecutionContext,
    recentAlbums: RecentAlbums,
    @NewDir newAlbumObservable: Observable[Album],
    webSocketFactory: PlayWebSocketRegistryFactory,
) {
  private def sinceDays(d: Int): Future[JsValue] = Future(recentAlbums.sinceDays(d)).map(_.jsonify)
  private def sinceMonths(m: Int): Future[JsValue] =
    Future(recentAlbums.sinceMonths(m)).map(_.jsonify)
  def since(dayString: String): Future[JsValue] = {
    val number = dayString.takeWhile(_.isDigit).toInt
    val last = dayString.last.toLower
    if (last == 'd' || last.isDigit)
      sinceDays(number)
    else {
      require(last == 'm', "Formats support are pure numbers, numbers ending in d, or ending in m")
      sinceMonths(number)
    }
  }

  private implicit val iec: ExecutionContext = ec

  def all(amount: Int): Future[JsValue] = Future(recentAlbums.all(amount)).map(_.jsonify)
  def double(amount: Int): Future[JsValue] = Future(recentAlbums.double(amount)).map(_.jsonify)
  def last: Future[JsValue] = Future(recentAlbums.all(1).single.jsonify)

  def debugLast(): JsValue =
    recentAlbums
      .all(1)
      .head
      .jsonify
      .<|(webSocketFactory(RecentModule.WebSocketName) broadcast _.toString)

  def register: WebSocketRefReader =
    Reader(ws => newAlbumObservable.doOnNext(ws broadcast _.jsonify.toString).subscribe())
}
