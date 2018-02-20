package common.json

import play.api.libs.json._

trait ToJsonableOps {
  implicit class parseString($: String) {
    def parseJsonable[T: Reads]: T = parseJsValue(Json.parse($)).parse[T]
  }
  implicit class jsonifySingle[T]($: T)(implicit ev: Writes[T]) {
    def jsonify: JsValue = ev writes $
  }
  implicit class parseJsValue($: JsValue) {
    def parse[T](implicit ev: Reads[T]): T = ev.reads($).get
  }
  implicit class parseArray($: JsArray) {
    def parse[T](implicit ev: Reads[T]): Seq[T] = $.value.map(ev.reads(_).get)
  }
}
