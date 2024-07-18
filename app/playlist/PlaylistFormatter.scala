package playlist

import javax.inject.{Inject, Singleton}

import controllers.{ControllerSongJsonifier, UrlPathUtils}
import models.Song
import play.api.libs.json.{JsArray, JsValue}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.DurationInt

import common.json.JsonReadable
import common.json.RichJson.DynamicJson
import common.json.ToJsonableOps.{jsonifyArray, jsonifySingle, parseArray, parseJsValue}
import common.rich.RichT.richT

private class PlaylistFormatter @Inject() (
    @Singleton $ : PlaylistModel,
    songJsonifier: ControllerSongJsonifier,
    urlPathUtils: UrlPathUtils,
    ec: ExecutionContext,
) {
  private implicit val iec: ExecutionContext = ec
  import songJsonifier.songJsonable
  // TODO (again) remove code duplication with JsonReadable[PlaylistState]
  private implicit val parseState: JsonReadable[Playlist] = json => {
    val songs = json.array("songs").parse[Song]
    val duration: Double = json.double("duration")
    val currentIndex: Int = json.int("currentIndex")
    Playlist(songs, currentIndex, duration.toInt.seconds)
  }
  def getIds: Future[JsArray] = $.getIds.map(_.toVector.jsonifyArray)
  def get(id: String): Future[Option[JsValue]] =
    $.get(id).map(_.map(_.jsonify).log("json of get: ".+))
  def set(id: String, value: JsValue): Future[Unit] = $.set(id, value.parse)
  def remove(id: String): Future[Boolean] = $.remove(id)
}
