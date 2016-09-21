package controllers

import common.io.JsonableSaver
import common.rich.RichT._
import play.api.libs.json.{JsArray, Json}
import play.api.mvc.{Action, AnyContent, Controller}
import playlist.{Playlist => PL}
import PL.PlaylistJsonable

object Playlist extends Controller {
  private implicit val c = PlayConfig
  import c._ // I have no idea why it doesn't require it
  private val saver = new JsonableSaver
  private def getStringFromBody(a: AnyContent): String = a.asFormUrlEncoded.get.keys.head
  def get = Action {
    Ok(saver.loadObject.songs map Player.toJson mapTo JsArray.apply)
  }
  def set = Action { request =>
    val playlist = request.body.mapTo(getStringFromBody)
        .mapTo(Json.parse).as[JsArray].value.map(_.as[String])
        .map(Utils.parseSong)
        .mapTo(PL.apply)
    saver save playlist
    Created.withHeaders("Location" -> "playlist")
  }
}
