package controllers

import common.rich.RichT._
import play.api.libs.json.JsArray
import play.api.mvc.{Action, Controller}
import playlist.{Playlist => PL}

object Playlist extends Controller {
  private implicit val c = PlayConfig
  import c._
  def get = Action {
    Ok(PL.load.songs map Player.toJson mapTo JsArray.apply)
  }
  def set = Action { request =>
    request.body.asJson.get.as[JsArray].value.map(_.as[String]) map Utils.parseSong mapTo PL.apply mapTo PL.save
    Created.withHeaders("Location" -> "playlist")
  }
}
