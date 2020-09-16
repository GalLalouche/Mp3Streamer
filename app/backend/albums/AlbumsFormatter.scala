package backend.albums

import backend.albums.filler.storage.FilledStorage
import backend.recon.Artist
import javax.inject.Inject
import mains.fixer.StringFixer
import play.api.libs.json.JsValue

import scala.concurrent.{ExecutionContext, Future}

import common.json.RichJson._
import common.json.ToJsonableOps._
import common.rich.RichT._

private class AlbumsFormatter @Inject()(ec: ExecutionContext, $: FilledStorage) {
  private implicit val iec: ExecutionContext = ec

  def albums: Future[JsValue] = $.all
      .map(
        _.map {case (artist, newAlbums) =>
          StringFixer(artist.name) -> newAlbums.map(NewAlbum.title.modify(_ tryOrKeep StringFixer.apply)).jsonify
        }.jsonify)

  def removeArtist(artistName: String): Future[_] = $.remove(Artist(artistName))
  def ignoreArtist(artistName: String): Future[_] = $.ignore(Artist(artistName))

  private def extractAlbum(json: JsValue): (Artist, String) =
    Artist(json str "artistName") -> json.str("title")
  def removeAlbum(json: JsValue): Future[_] = {
    val (artist, album) = extractAlbum(json)
    $.remove(artist, album)
  }
  def ignoreAlbum(json: JsValue): Future[_] = {
    val (artist, album) = extractAlbum(json)
    $.ignore(artist, album)
  }
}
