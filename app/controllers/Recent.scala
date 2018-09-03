package controllers

import java.time.ZoneOffset

import backend.logging.Logger
import common.io.DirectoryRef
import common.json.ToJsonableOps
import controllers.websockets.WebSocketController
import javax.inject.Inject
import models.{Album, AlbumFactory, MusicFinder}
import models.ModelJsonable.AlbumJsonifier

import scala.concurrent.{ExecutionContext, Future}

class Recent @Inject()(
    logger: Logger,
    ec: ExecutionContext,
    albumFactory: AlbumFactory,
    mf: MusicFinder,
) extends WebSocketController(logger) with ToJsonableOps {
  private implicit val iec: ExecutionContext = ec
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

  def newDir(dir: DirectoryRef) = broadcast(albumFactory.fromDir(dir).jsonify.toString)
}
