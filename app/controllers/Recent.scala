package controllers

import java.time.ZoneOffset

import models.Album
import play.api.mvc.{Action, Controller}
import search.ModelJsonable.AlbumJsonifier

import scala.concurrent.Future

object Recent extends Controller {
  import Utils.config._
  // TODO move to a backend class
  private def recentAlbums(amount: Int): Future[Seq[Album]] = Future {
    Utils.config.mf.genreDirs // mf is inlined because otherwise it doesn't pick up the implicit :(
        .flatMap(_.deepDirs)
        .filter(e => Utils.config.mf.getSongFilesInDir(e).nonEmpty)
        .sortBy(_.lastModified)(Ordering.by(-_.toEpochSecond(ZoneOffset.UTC)))
        .map(Album.apply)
        .take(amount)
  }
  def recent(amount: Int) = Action.async {
    recentAlbums(amount).map(AlbumJsonifier.jsonify).map(Ok(_))
  }
  def last = Action.async {
    recentAlbums(1).map(AlbumJsonifier.jsonify).map(Ok(_))
  }
}
