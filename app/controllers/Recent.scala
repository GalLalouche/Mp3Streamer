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
    ControllerUtils.config.mf.genreDirs // mf is inlined because otherwise it doesn't pick up the implicit :(
        .flatMap(_.deepDirs)
        .filter(e => ControllerUtils.config.mf.getSongFilesInDir(e).nonEmpty)
        .sortBy(_.lastModified)(Ordering.by(-_.toEpochSecond(ZoneOffset.UTC)))
        .take(amount)
        .map(Album.apply)
        .map(Album.songs.set(Seq())) // recent doesn't care about songs
  }
  def recent(amount: Int) = Action.async {
    recentAlbums(amount).map(_.jsonify).map(Ok(_))
  }
  def last = Action.async {
    recentAlbums(1).map(_.jsonify).map(Ok(_))
  }

  def newDir(dir: DirectoryRef) = broadcast(Album(dir).jsonify.toString)
}
