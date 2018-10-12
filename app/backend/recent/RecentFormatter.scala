package backend.recent

import common.json.ToJsonableOps
import controllers.websockets.WebSocketRef.WebSocketRefReader
import javax.inject.Inject
import models.Album
import models.ModelJsonable.AlbumJsonifier
import play.api.libs.json.JsValue
import play.api.mvc.InjectedController
import rx.lang.scala.Observable

import scala.concurrent.{ExecutionContext, Future}

import scalaz.Reader

class RecentFormatter @Inject()(
    ec: ExecutionContext,
    recentAlbums: RecentAlbums,
    @NewDir newAlbumObservable: Observable[Album]
) extends InjectedController with ToJsonableOps {
  private implicit val iec: ExecutionContext = ec

  def recent(amount: Int): Future[JsValue] = Future(recentAlbums(amount)).map(_.jsonify)
  def last: Future[JsValue] = recent(1)

  def register: WebSocketRefReader = Reader(ws => newAlbumObservable.doOnNext(ws broadcast _.jsonify.toString))
}
