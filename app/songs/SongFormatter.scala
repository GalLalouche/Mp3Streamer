package songs

import com.google.inject.Inject
import formatter.ControllerSongJsonifier
import play.api.libs.json.JsValue

import scala.language.implicitConversions

import common.json.ToJsonableOps._

class SongFormatter @Inject() ($ : SongModel, songJsonifier: ControllerSongJsonifier) {
  import songJsonifier.songJsonable

  def randomSong(): JsValue = $.randomSong().jsonify
  def randomMp3Song(): JsValue = $.randomMp3Song().jsonify
  def randomFlacSong(): JsValue = $.randomFlacSong().jsonify

  def album(path: String): JsValue = $.album(path).jsonify
  def discNumber(path: String, requestedDiscNumber: String): JsValue =
    $.discNumber(path, requestedDiscNumber).jsonify

  def song(path: String): JsValue = $.song(path).jsonify
  def nextSong(path: String): JsValue = $.nextSong(path).jsonify
}
