package controllers

import java.io.File
import java.time.ZoneOffset

import backend.albums.{NewAlbum, NewAlbums}
import backend.recon._
import common.Debug
import common.RichJson._
import common.io.DirectoryRef
import common.rich.RichT._
import models.Song
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.Future
import scalaz.std.FutureInstances
import scalaz.syntax.ToFunctorOps

/** A web interface to new albums finder. Displays new albums and can update the current file / ignoring policy. */
//noinspection MutatorLikeMethodIsParameterless
object NewAlbums extends Controller with Debug
    with FutureInstances with ToFunctorOps {
  import Utils.config
  private val $ = new NewAlbums()

  private def toJson(a: NewAlbum): JsObject =
    Json.obj("title" -> a.title, "year" -> a.year, "type" -> a.albumType)
  private def toJson(a: Artist, albums: Seq[NewAlbum]): JsObject =
    Json.obj("artistName" -> a.name, "albums" -> JsArray(albums map toJson))
  private def toJson(m: Map[Artist, Seq[NewAlbum]]): JsArray =
    m.map(e => toJson(e._1, e._2)).toSeq |> JsArray.apply

  def albums = Action.async {
    $.load.map(toJson).map(Ok(_))
  }

  private def updateNewAlbums[A <: Reconcilable](extractor: Request[AnyContent] => A, action: A => Future[Unit]) =
    Action.async {request =>
      action(extractor(request)).>|(NoContent)
    }

  private def extractArtist(request: Request[AnyContent]): Artist = request.body.asText.get |> Artist
  def removeArtist = updateNewAlbums(extractArtist, $.removeArtist)
  def ignoreArtist = updateNewAlbums(extractArtist, $.ignoreArtist)

  private def extractAlbum(request: Request[AnyContent]): Album = {
    val json = request.body.asJson.get
    Album(json str "title", json int "year", Artist(json str "artistName"))
  }
  def removeAlbum = updateNewAlbums(extractAlbum, $.removeAlbum)
  def ignoreAlbum = updateNewAlbums(extractAlbum, $.ignoreAlbum)

  def index = Action {
    Ok(views.html.new_albums())
  }
}