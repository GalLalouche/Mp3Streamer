package controllers

import java.time.ZoneOffset

import common.io.DirectoryRef
import common.json.ToJsonableOps
import controllers.websockets.WebSocketController
import models.Album
import models.ModelJsonable.AlbumJsonifier
import play.api.mvc.Action

import scala.concurrent.Future

object Recent extends WebSocketController with ToJsonableOps {
  // TODO move to a backend class
  private def recentAlbums(amount: Int): Future[Seq[Album]] = Future {
    val mf = ControllerUtils.config.mf
    mf.genreDirs
        .flatMap(_.deepDirs)
        .filter(mf.getSongFilesInDir(_).nonEmpty)
        .sortBy(_.lastModified)(Ordering.by(-_.toEpochSecond(ZoneOffset.UTC)))
        .take(amount)
        .map(Album.apply)
        .map(Album.songs set Nil) // recent doesn't care about songs
  }
  def recent(amount: Int) = Action.async {
    recentAlbums(amount).map(_.jsonify).map(Ok(_))
  }
  def last = Action.async {
    recentAlbums(1).map(_.jsonify).map(Ok(_))
  }

  def newDir(dir: DirectoryRef) = broadcast(Album(dir).jsonify.toString)
}
