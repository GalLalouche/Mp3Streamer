package controllers

import java.time.ZoneOffset

import common.io.DirectoryRef
import common.json.ToJsonableOps
import controllers.websockets.WebSocketController
import models.{Album, AlbumFactory, MusicFinder}
import models.ModelJsonable.AlbumJsonifier
import net.codingwell.scalaguice.InjectorExtensions._
import play.api.mvc.Action

import scala.concurrent.{ExecutionContext, Future}

object Recent extends WebSocketController with ToJsonableOps {
  private implicit val ec: ExecutionContext = c.injector.instance[ExecutionContext]
  private val albumFactory = c.injector.instance[AlbumFactory]
  // TODO move to a backend class
  private def recentAlbums(amount: Int): Future[Seq[Album]] = Future {
    val mf = c.injector.instance[MusicFinder]
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
