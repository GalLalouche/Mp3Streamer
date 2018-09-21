package backend.recent

import common.json.ToJsonableOps
import controllers.websockets.WebSocketRegistryFactory
import javax.inject.Inject
import models.Album
import models.ModelJsonable.AlbumJsonifier
import play.api.mvc.{InjectedController, WebSocket}
import rx.lang.scala.Observable

import scala.concurrent.{ExecutionContext, Future}

class RecentController @Inject()(
    ec: ExecutionContext,
    webSocketFactory: WebSocketRegistryFactory,
    recentAlbums: RecentAlbums,
    @NewDir newAlbumObservable: Observable[Album]
) extends InjectedController with ToJsonableOps {
  private implicit val iec: ExecutionContext = ec
  private val webSocket = webSocketFactory("Recent")

  def recent(amount: Int) = Action.async {
    Future(recentAlbums(amount)).map(Ok apply _.jsonify)
  }
  def last = recent(1)

  newAlbumObservable.doOnNext(webSocket broadcast _.jsonify.toString).subscribe()
  def accept(): WebSocket = webSocket.accept()
}
