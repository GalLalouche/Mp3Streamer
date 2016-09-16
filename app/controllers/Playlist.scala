package controllers

import common.rich.RichT._
import play.api.libs.json.{JsArray, Json}
import play.api.mvc.{Action, AnyContent, Controller}
import playlist.{Playlist => PL}

object Playlist extends Controller {
  private implicit val c = PlayConfig
  import c._
  // I have no idea :|
  private def getStringFromBody(a: AnyContent): String = a.asFormUrlEncoded.get.keys.head
  def get = Action {
    Ok(PL.load.songs map Player.toJson mapTo JsArray.apply)
  }
  def set = Action { request =>
    request.body.mapTo(getStringFromBody)
        .mapTo(Json.parse).as[JsArray].value.map(_.as[String])
        .map(Utils.parseSong)
        .mapTo(PL.apply)
        .mapTo(PL.save)
    Created.withHeaders("Location" -> "playlist")
  }
}
