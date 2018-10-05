package controllers

import common.RichJson._
import common.json.{Jsonable, JsonableOverrider, OJsonableOverrider}
import common.rich.path.RichFile._
import models.{IOSong, Poster, Song}
import models.ModelJsonable._
import play.api.libs.json.{JsObject, JsString}
import play.api.mvc.Request

// TODO inject this
object ControllerUtils {
  implicit val songJsonable: Jsonable[Song] = JsonableOverrider[Song](new OJsonableOverrider[Song] {
    override def jsonify(s: Song, original: => JsObject) = original +
        ("file" -> JsString(PlayUrlPathUtils.encodePath(s))) +
        ("poster" -> JsString("/posters/" + Poster.getCoverArt(s.asInstanceOf[IOSong]).path)) +
        (s.file.extension -> JsString("/stream/download/" + PlayUrlPathUtils.encodePath(s)))
    override def parse(obj: JsObject, unused: => Song) =
      SongJsonifier.parse(obj + ("file" -> JsString(PlayUrlPathUtils.decode(obj str "file"))))
  })

  def shouldEncodeMp3(request: Request[_]): Boolean =
    request.headers("User-Agent").contains("Chrome") || request.headers.get("Referer").exists(_ endsWith "mp3")
}
