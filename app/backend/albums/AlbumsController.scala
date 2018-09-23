package backend.albums

import backend.Retriever
import backend.recon._
import common.Debug
import common.RichJson._
import common.json.ToJsonableOps
import common.rich.RichT._
import javax.inject.Inject
import mains.fixer.StringFixer
import play.api.libs.json.JsValue
import play.api.mvc.{AnyContent, InjectedController, Request}

import scala.concurrent.ExecutionContext

import scalaz.std.FutureInstances
import scalaz.syntax.ToFunctorOps

/** A web interface to new albums finder. Displays new albums and can update the current file / ignoring policy. */
class AlbumsController @Inject()(ec: ExecutionContext, $: NewAlbums) extends InjectedController with Debug
    with FutureInstances with ToFunctorOps with ToJsonableOps {
  private implicit val iec: ExecutionContext = ec

  def albums = Action.async {
    $.loadAlbumsByArtist
        .map(
          _.map {
            case (artist, newAlbums) =>
              artist.name -> newAlbums.map(NewAlbum.title.modify(_ tryOrKeep StringFixer.apply)).jsonify
          }.jsonify)
        .map(Ok(_))
  }

  private def updateNewAlbums[A](
      extract: Request[AnyContent] => A, act: Retriever[A, Unit]) = Action.async {
    _ |> extract |> act as NoContent
  }

  private def extractArtist(request: Request[AnyContent]): Artist =
    request.body.asText.get |> Artist.apply
  def removeArtist() = updateNewAlbums(extractArtist, $.removeArtist)
  def ignoreArtist() = updateNewAlbums(extractArtist, $.ignoreArtist)

  private def extractAlbum(request: Request[AnyContent]): Album = {
    val json = request.body.asJson.get
    Album(json str "title", json int "year", Artist(json str "artistName"))
  }
  def removeAlbum() = updateNewAlbums(extractAlbum, $.removeAlbum)
  def ignoreAlbum() = updateNewAlbums(extractAlbum, $.ignoreAlbum)

  def index = Action {
    Ok(views.html.new_albums())
  }
}
