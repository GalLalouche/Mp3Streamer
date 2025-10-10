package backend.new_albums

import backend.recon.{Artist, ReconID}
import com.google.inject.Inject
import mains.fixer.StringFixer
import models.TypeAliases.ArtistName
import play.api.libs.json.{JsArray, Json, JsString, JsValue}

import scala.concurrent.{ExecutionContext, Future}

import cats.syntax.functor.toFunctorOps
import common.rich.func.kats.ToMoreFoldableOps._
import monocle.Monocle.toApplyTraversalOps
import monocle.Traversal

import common.json.JsonWriteable
import common.json.RichJson._
import common.json.ToJsonableOps._
import common.rich.RichT._

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
      "name" -> stringFixer(a.artist.name), // Name is stored normalized.
      "artistScore" -> a.artistScore.entryName,
      "albums" -> fixTitles(a.albums).jsonify,
    )
  }
  def albums: Future[JsValue] = $.albums.map(_.jsonify).value.map(e => JsArray.apply(e.toArray))

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
  def removeAlbum(albumReconId: String): Future[_] =
    $.removeAlbum(ReconID.validateOrThrow(albumReconId))
  def ignoreAlbum(albumReconId: String): Future[_] =
    $.ignoreAlbum(ReconID.validateOrThrow(albumReconId))
}
