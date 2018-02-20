package backend.albums

import backend.recon._
import common.Debug
import common.RichJson._
import common.json.Jsonable.ToJsonableOps
import common.rich.RichT._
import common.rich.collections.RichMap._
import controllers.LegacyController
import play.api.mvc.{Action, AnyContent, Request}

import scala.concurrent.Future
import scalaz.std.FutureInstances
import scalaz.syntax.ToFunctorOps

/** A web interface to new albums finder. Displays new albums and can update the current file / ignoring policy. */
object AlbumsController extends LegacyController with Debug
    with FutureInstances with ToFunctorOps with ToJsonableOps {
  private val $ = new NewAlbums()

  def albums = Action.async {
    $.load.map(Ok apply _.mapKeys(_.name).jsonify)
  }

  private def updateNewAlbums[A](extract: Request[AnyContent] => A, act: A => Future[Unit]) = Action.async {
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
