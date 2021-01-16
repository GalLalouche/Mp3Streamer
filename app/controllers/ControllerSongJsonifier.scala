package controllers

import javax.inject.Inject
import models.{IOSong, ModelJsonable, Poster, Song}
import models.ModelJsonable.SongJsonifier
import play.api.libs.json.{JsObject, JsString}

import common.io.IOFile
import common.json.{JsonableOverrider, OJsonable, OJsonableOverrider}
import common.json.RichJson._
import common.rich.RichT._

class ControllerSongJsonifier @Inject()(urlPathUtils: UrlPathUtils) {
  implicit val songJsonable: OJsonable[Song] = JsonableOverrider[Song](new OJsonableOverrider[Song] {
    override def jsonify(s: Song, original: => JsObject) = {
      val posterPath = urlPathUtils.encodePath(Poster.getCoverArt(s.asInstanceOf[IOSong]) |> IOFile.apply)
      val $ = original +
          ("file" -> JsString(urlPathUtils.encodePath(s))) +
          ("poster" -> JsString("/posters/" + posterPath)) +
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
