package backend.albums

import backend.recon.{Album, Artist}
import javax.inject.Inject
import mains.fixer.StringFixer
import play.api.libs.json.JsValue

import scala.concurrent.{ExecutionContext, Future}

import common.RichJson._
import common.json.ToJsonableOps._
import common.rich.RichT._

private class AlbumsFormatter @Inject()(ec: ExecutionContext, $: NewAlbums) {
  private implicit val iec: ExecutionContext = ec

  def albums: Future[JsValue] = $.loadAlbumsByArtist
      .map(
        _.map {case (artist, newAlbums) =>
          artist.name -> newAlbums.map(NewAlbum.title.modify(_ tryOrKeep StringFixer.apply)).jsonify
        }.jsonify)

  def removeArtist(artistName: String): Future[_] = $.removeArtist(Artist(artistName))
  def ignoreArtist(artistName: String): Future[_] = $.ignoreArtist(Artist(artistName))

  private def extractAlbum(json: JsValue): Album =
    Album(json str "title", json int "year", Artist(json str "artistName"))
  def removeAlbum(album: JsValue): Future[_] = $.removeAlbum(extractAlbum(album))
  def ignoreAlbum(album: JsValue): Future[_] = $.ignoreAlbum(extractAlbum(album))
}
