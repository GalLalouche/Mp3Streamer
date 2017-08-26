package controllers

import java.time.ZoneOffset

import common.Jsonable
import common.io.DirectoryRef
import controllers.websockets.WebSocketController
import models.Album
import play.api.mvc.Action
import models.ModelJsonable.AlbumJsonifier

import scala.concurrent.Future

object Recent extends WebSocketController with Jsonable.ToJsonableOps {
  import Utils.config._
  // TODO move to a backend class
  private def recentAlbums(amount: Int): Future[Seq[Album]] = Future {
    Utils.config.mf.genreDirs // mf is inlined because otherwise it doesn't pick up the implicit :(
        .flatMap(_.deepDirs)
        .filter(e => Utils.config.mf.getSongFilesInDir(e).nonEmpty)
        .sortBy(_.lastModified)(Ordering.by(-_.toEpochSecond(ZoneOffset.UTC)))
        .take(amount)
        .map(Album.apply)
  }
  def recent(amount: Int) = Action.async {
    recentAlbums(amount).map(_.jsonify).map(Ok(_))
  }
  def last = Action.async {
    recentAlbums(1).map(_.jsonify).map(Ok(_))
  }

  def newDir(dir: DirectoryRef) = broadcast(Album(dir).jsonify.toString)
}
