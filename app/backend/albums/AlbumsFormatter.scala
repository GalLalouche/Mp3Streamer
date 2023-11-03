package backend.albums

import backend.recon.Artist
import javax.inject.Inject
import mains.fixer.StringFixer
import play.api.libs.json.{JsArray, Json, JsString, JsValue}

import scala.concurrent.{ExecutionContext, Future}

import scalaz.std.option.optionInstance
import common.rich.func.BetterFutureInstances._
import common.rich.func.MoreTraverseInstances._
import common.rich.func.ToMoreFoldableOps._
import monocle.Monocle.toApplyTraversalOps
import monocle.Traversal

import common.json.JsonWriteable
import common.json.RichJson._
import common.json.ToJsonableOps._
import common.rich.RichT._

// FIXME This module returns StringFixed titles, but then we try to remove/ignore those entities, the name
//  doesn't match the name in the database. A simple solution could be to use the original recon ID for that.
private class AlbumsFormatter @Inject() (
    ec: ExecutionContext,
    $ : AlbumsModel,
    stringFixer: StringFixer,
) {
  private implicit val iec: ExecutionContext = ec

  private def fixTitles: Seq[NewAlbum] => Seq[NewAlbum] =
    _.&|->>(Traversal.fromTraverse).^|->(NewAlbum.title).modify(_.tryOrKeep(stringFixer.apply))
  private implicit object ArtistAlbumsJsonable extends JsonWriteable[AlbumsModel.ModelResult] {
    override def jsonify(a: AlbumsModel.ModelResult) = Json.obj(
      "genre" -> a.genre.mapHeadOrElse(_.name, "N/A"),
      "name" -> StringFixer(a.artist.name), // Name is stored normalized.
      "artistScore" -> a.artistScore.orDefaultString,
      "albums" -> fixTitles(a.albums).jsonify,
    )
  }
  def albums: Future[JsValue] = $.albums.map(_.jsonify).run.map(JsArray.apply)

  def forArtist(artistName: String): Future[JsValue] = $.forArtist(artistName).map {
    case AlbumsModel.NonIgnoredArtist(albums) => fixTitles(albums).jsonify
    case AlbumsModel.IgnoredArtist => JsString("IGNORED")
  }
  def removeArtist(artistName: String): Future[_] = $.removeArtist(artistName)
  def ignoreArtist(artistName: String): Future[_] = $.ignoreArtist(artistName)
  def unignoreArtist(artistName: String): Future[Seq[NewAlbum]] = $.unignoreArtist(artistName)

  private def extractAlbum(json: JsValue): (Artist, String) =
    Artist(json.str("artistName")) -> json.str("title")
  def removeAlbum(json: JsValue): Future[_] = {
    val (artist, album) = extractAlbum(json)
    $.removeAlbum(artist, album)
  }
  def ignoreAlbum(json: JsValue): Future[_] = {
    val (artist, album) = extractAlbum(json)
    $.ignoreAlbum(artist, album)
  }
}
