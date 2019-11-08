package controllers

import javax.inject.Inject
import models.{IOSong, ModelJsonable, Poster, Song}
import models.ModelJsonable.SongJsonifier
import play.api.libs.json.{JsObject, JsString}

import common.json.{JsonableOverrider, OJsonable, OJsonableOverrider}
import common.RichJson._
import common.rich.path.RichFile._

class ControllerSongJsonifier @Inject()(urlPathUtils: UrlPathUtils) {
  implicit val songJsonable: OJsonable[Song] = JsonableOverrider[Song](new OJsonableOverrider[Song] {
    override def jsonify(s: Song, original: => JsObject) = {
      val $ = original +
          ("file" -> JsString(urlPathUtils.encodePath(s))) +
          ("poster" -> JsString("/posters/" + Poster.getCoverArt(s.asInstanceOf[IOSong]).path)) +
          (s.file.extension -> JsString("/stream/download/" + urlPathUtils.encodePath(s)))
      $.append("compopser" -> s.composer)
          .append("conductor" -> s.conductor)
          .append("orchestra" -> s.orchestra)
          .append("opus" -> s.opus)
          .append("performanceYear" -> s.performanceYear)
    }
    override def parse(obj: JsObject, unused: => Song) =
      SongJsonifier.parse(obj + ("file" -> JsString(urlPathUtils.decode(obj str "file"))))
  })(ModelJsonable.SongJsonifier)

  def apply(s: Song): JsObject = songJsonable.jsonify(s)
}
