package backend.albums

import backend.recon.Artist
import javax.inject.Inject
import mains.fixer.StringFixer
import play.api.libs.json.{JsArray, Json, JsValue}

import scala.concurrent.{ExecutionContext, Future}

import scalaz.std.option.optionInstance
import common.rich.func.BetterFutureInstances._
import common.rich.func.ToMoreFoldableOps._

import common.json.RichJson._
import common.json.ToJsonableOps._
import common.rich.RichT._

private class AlbumsFormatter @Inject()(
    ec: ExecutionContext,
    $: AlbumsModel,
) {
  private implicit val iec: ExecutionContext = ec

  def albums: Future[JsValue] = $.albumsFiltered
      .map {case AlbumsModel.ArtistAlbums(artist, modelScore, newAlbums, genre) => Json.obj(
        "genre" -> genre.mapHeadOrElse(_.name, "N/A"),
        "name" -> StringFixer(artist.name), // Name is stored normalized.
        "artistScore" -> modelScore.orDefaultString,
        "albums" -> newAlbums.map(NewAlbum.title.modify(_ tryOrKeep StringFixer.apply)).jsonify,
      )
      }.run
      .map(JsArray.apply)

  def removeArtist(artistName: String): Future[_] = $.removeArtist(artistName)
  def ignoreArtist(artistName: String): Future[_] = $.ignoreArtist(artistName)

  private def extractAlbum(json: JsValue): (Artist, String) =
    Artist(json str "artistName") -> json.str("title")
  def removeAlbum(json: JsValue): Future[_] = {
    val (artist, album) = extractAlbum(json)
    $.removeAlbum(artist, album)
  }
  def ignoreAlbum(json: JsValue): Future[_] = {
    val (artist, album) = extractAlbum(json)
    $.removeAlbum(artist, album)
  }
}
