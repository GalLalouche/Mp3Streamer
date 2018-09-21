package controllers

import java.time.ZoneOffset

import common.io.DirectoryRef
import common.json.ToJsonableOps
import controllers.websockets.WebSocketRegistryFactory
import javax.inject.Inject
import models.{Album, AlbumFactory, MusicFinder}
import models.ModelJsonable.AlbumJsonifier
import play.api.mvc.{InjectedController, WebSocket}

import scala.concurrent.{ExecutionContext, Future}

class RecentController @Inject()(
    ec: ExecutionContext,
    albumFactory: AlbumFactory,
    mf: MusicFinder,
    webSocketFactory: WebSocketRegistryFactory
) extends InjectedController with ToJsonableOps {
  private implicit val iec: ExecutionContext = ec
  private val webSocket = webSocketFactory("Recent")
  // TODO move to a backend class
  private def recentAlbums(amount: Int): Future[Seq[Album]] = Future {
    mf.genreDirs
        .flatMap(_.deepDirs)
        .filter(mf.getSongFilesInDir(_).nonEmpty)
        .sortBy(_.lastModified)(Ordering.by(-_.toEpochSecond(ZoneOffset.UTC)))
        .take(amount)
        .map(albumFactory.fromDir)
        .map(Album.songs set Nil) // recent doesn't care about songs
  }
  def recent(amount: Int) = Action.async {
    recentAlbums(amount).map(_.jsonify).map(Ok(_))
  }
  def last = Action.async {
    recentAlbums(1).map(_.jsonify).map(Ok(_))
  }

  def newDir(dir: DirectoryRef) = webSocket.broadcast(albumFactory.fromDir(dir).jsonify.toString)
  def accept(): WebSocket = webSocket.accept()
}
