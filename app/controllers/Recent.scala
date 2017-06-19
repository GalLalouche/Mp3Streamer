package controllers

import java.time.ZoneOffset

import models.Album
import play.api.mvc.{Action, Controller}
import search.ModelsJsonable.AlbumJsonifier

import scala.concurrent.Future

object Recent extends Controller {
  import Utils.config._
  private val mf = Utils.config.mf
  // TODO move to a backend class
  private def recentAlbums(amount: Int): Future[Seq[Album]] = {
    Future(mf.genreDirs
        .flatMap(_.deepDirs)
        .filter(e => mf.getSongFilesInDir(e).nonEmpty)
        .sortBy(_.lastModified)(Ordering.by(-_.toEpochSecond(ZoneOffset.UTC)))
        .map(_.dir)
        .map(Album.apply)
        .take(amount)
    )
  }
  def recent(amount: Int) = Action.async {
    recentAlbums(amount).map(AlbumJsonifier.jsonify).map(Ok(_))
  }
  def last = Action.async {
    recentAlbums(1).map(AlbumJsonifier.jsonify).map(Ok(_))
  }
}
