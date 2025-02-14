package playlist

import javax.inject.Inject

import formatter.ControllerSongJsonifier
import play.api.libs.json.{JsArray, JsValue}

import scala.concurrent.{ExecutionContext, Future}

import common.json.ToJsonableOps.{jsonifyArray, jsonifySingle, parseJsValue}

class PlaylistFormatter @Inject() (
    $ : PlaylistModel,
    songJsonifier: ControllerSongJsonifier,
    ec: ExecutionContext,
) {
  private implicit val iec: ExecutionContext = ec
  import songJsonifier.songJsonable
  def getIds: Future[JsArray] = $.getIds.map(_.toVector.sorted.jsonifyArray)
  def get(id: String): Future[Option[JsValue]] = $.get(id).map(_.map(_.jsonify))
  def set(id: String, value: JsValue): Future[Unit] = $.set(id, value.parse[Playlist])
  def remove(id: String): Future[Boolean] = $.remove(id)
}
