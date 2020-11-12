package backend.recent

import controllers.websockets.WebSocketRef.WebSocketRefReader
import javax.inject.Inject
import models.Album
import models.ModelJsonable.AlbumJsonifier
import play.api.libs.json.JsValue
import play.api.mvc.InjectedController
import rx.lang.scala.Observable

import scala.concurrent.{ExecutionContext, Future}

import scalaz.Reader

import common.json.ToJsonableOps._

private class RecentFormatter @Inject()(
    ec: ExecutionContext,
    recentAlbums: RecentAlbums,
    @NewDir newAlbumObservable: Observable[Album],
) extends InjectedController {
  private implicit val iec: ExecutionContext = ec

  def all(amount: Int): Future[JsValue] = Future(recentAlbums.all(amount)).map(_.jsonify)
  def double(amount: Int): Future[JsValue] = Future(recentAlbums.double(amount)).map(_.jsonify)
  def last: Future[JsValue] = all(1)

  def register: WebSocketRefReader =
    Reader(ws => newAlbumObservable.doOnNext(ws broadcast _.jsonify.toString).subscribe())
}
