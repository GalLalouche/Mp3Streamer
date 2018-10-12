package backend.albums

import backend.recon.{Album, Artist}
import common.RichJson._
import common.json.ToJsonableOps
import common.rich.RichT._
import javax.inject.Inject
import mains.fixer.StringFixer
import play.api.libs.json.{JsObject, JsValue}

import scala.concurrent.{ExecutionContext, Future}

import scalaz.std.FutureInstances
import scalaz.syntax.ToFunctorOps

class AlbumsFormatter @Inject()(ec: ExecutionContext, $: NewAlbums)
    extends ToFunctorOps with FutureInstances
        with ToJsonableOps {
  private implicit val iec: ExecutionContext = ec

  def albums: Future[JsValue] = $.loadAlbumsByArtist
      .map(
        _.map {
          case (artist, newAlbums) =>
            artist.name -> newAlbums.map(NewAlbum.title.modify(_ tryOrKeep StringFixer.apply)).jsonify
        }.jsonify)

  def removeArtist(artistName: String): Future[_] = $.removeArtist(Artist(artistName))
  def ignoreArtist(artistName: String): Future[_] = $.ignoreArtist(Artist(artistName))

  private def extractAlbum(json: JsValue): Album =
    Album(json str "title", json int "year", Artist(json str "artistName"))
  def removeAlbum(album: JsValue): Future[_] = $.removeAlbum(extractAlbum(album))
  def ignoreAlbum(album: JsValue): Future[_] = $.ignoreAlbum(extractAlbum(album))
}
