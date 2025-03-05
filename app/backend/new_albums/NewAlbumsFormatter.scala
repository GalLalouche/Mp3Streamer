package backend.new_albums

import backend.recon.Artist
import com.google.inject.Inject
import mains.fixer.StringFixer
import models.TypeAliases.ArtistName
import play.api.libs.json.{JsArray, Json, JsString, JsValue}

import scala.concurrent.{ExecutionContext, Future}

import common.rich.func.BetterFutureInstances._
import common.rich.func.MoreTraverseInstances._
import common.rich.func.ToMoreFoldableOps._
import monocle.Monocle.toApplyTraversalOps
import monocle.Traversal
import scalaz.std.option.optionInstance

import common.json.JsonWriteable
import common.json.RichJson._
import common.json.ToJsonableOps._
import common.rich.RichT._

// FIXME This module returns StringFixed titles, but then we try to remove/ignore those entities, the name
//  doesn't match the name in the database. A simple solution could be to use the original recon ID for that.
class NewAlbumsFormatter @Inject() (
    ec: ExecutionContext,
    $ : NewAlbumsModel,
    stringFixer: StringFixer,
) {
  private implicit val iec: ExecutionContext = ec

  private def fixTitles: Seq[NewAlbum] => Seq[NewAlbum] =
    _.&|->>(Traversal.fromTraverse).^|->(NewAlbum.title).modify(_.tryOrKeep(stringFixer.apply))
  private implicit object ArtistAlbumsJsonable extends JsonWriteable[NewAlbumsModel.ModelResult] {
    override def jsonify(a: NewAlbumsModel.ModelResult) = Json.obj(
      "genre" -> a.genre.mapHeadOrElse(_.name, "N/A"),
      "name" -> StringFixer(a.artist.name), // Name is stored normalized.
      "artistScore" -> a.artistScore.entryName,
      "albums" -> fixTitles(a.albums).jsonify,
    )
  }
  def albums: Future[JsValue] = $.albums.map(_.jsonify).run.map(JsArray.apply)

  def forArtist(artistName: ArtistName): Future[Option[JsValue]] =
    $.forArtist(Artist(artistName)).map {
      case NewAlbumsModel.NonIgnoredArtist(albums) => Some(fixTitles(albums).jsonify)
      case NewAlbumsModel.IgnoredArtist => Some(JsString("IGNORED"))
      case NewAlbumsModel.Unreconciled => None
    }
  def removeArtist(artistName: ArtistName): Future[_] = $.removeArtist(Artist(artistName))
  def ignoreArtist(artistName: ArtistName): Future[_] = $.ignoreArtist(Artist(artistName))
  def unignoreArtist(artistName: ArtistName): Future[Seq[NewAlbum]] =
    $.unignoreArtist(Artist(artistName))

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
