package backend.recent

import common.io.DirectoryRef
import common.json.ToJsonableOps
import controllers.websockets.WebSocketRegistryFactory
import javax.inject.Inject
import models.AlbumFactory
import models.ModelJsonable.AlbumJsonifier
import play.api.mvc.{InjectedController, WebSocket}

import scala.concurrent.{ExecutionContext, Future}

class RecentController @Inject()(
    ec: ExecutionContext,
    webSocketFactory: WebSocketRegistryFactory,
    albumFactory: AlbumFactory,
    recentAlbums: RecentAlbums
) extends InjectedController with ToJsonableOps {
  private implicit val iec: ExecutionContext = ec
  private val webSocket = webSocketFactory("Recent")

  def recent(amount: Int) = Action.async {
    Future(recentAlbums(amount)).map(Ok apply _.jsonify)
  }
  def last = recent(1)

  def newDir(dir: DirectoryRef) = webSocket.broadcast(albumFactory.fromDir(dir).jsonify.toString)
  def accept(): WebSocket = webSocket.accept()
}
