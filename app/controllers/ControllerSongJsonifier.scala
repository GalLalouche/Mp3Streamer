package controllers

import common.json.{JsonableOverrider, OJsonable, OJsonableOverrider}
import common.RichJson._
import common.rich.path.RichFile._
import common.rich.RichT._
import common.rich.func.ToMoreFoldableOps
import javax.inject.Inject
import models.{IOSong, ModelJsonable, Poster, Song}
import models.ModelJsonable.SongJsonifier
import play.api.libs.json.{JsNumber, JsObject, JsString}

import scalaz.std.OptionInstances

class ControllerSongJsonifier @Inject()(urlPathUtils: UrlPathUtils)
    extends ToMoreFoldableOps with OptionInstances {
  implicit val songJsonable: OJsonable[Song] = JsonableOverrider[Song](new OJsonableOverrider[Song] {
    private def appendOptional($: JsObject, key: String, o: Option[Int]): JsObject =
      o.mapHeadOrElse(i => $ + (key -> JsNumber(i)), $)
    private def appendOptional($: JsObject, key: String, o: Option[String])(implicit d: DummyImplicit): JsObject =
      o.mapHeadOrElse(s => $ + (key -> JsString(s)), $)
    override def jsonify(s: Song, original: => JsObject) = {
      val $ = original +
          ("file" -> JsString(urlPathUtils.encodePath(s))) +
          ("poster" -> JsString("/posters/" + Poster.getCoverArt(s.asInstanceOf[IOSong]).path)) +
          (s.file.extension -> JsString("/stream/download/" + urlPathUtils.encodePath(s)))
      appendOptional($, "composer", s.composer)
          .mapTo(appendOptional(_, "opus", s.opus))
          .mapTo(appendOptional(_, "performanceYear", s.performanceYear))
    }
    override def parse(obj: JsObject, unused: => Song) =
      SongJsonifier.parse(obj + ("file" -> JsString(urlPathUtils.decode(obj str "file"))))
  })(ModelJsonable.SongJsonifier)

  def apply(s: Song): JsObject = songJsonable.jsonify(s)
}
