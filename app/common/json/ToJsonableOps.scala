package common.json

import play.api.libs.json._

trait ToJsonableOps {
  implicit class parseString($: String) {
    def parseJsonable[T: JsonReadable]: T = parseJsValue(Json.parse($)).parse[T]
  }
  implicit class jsonifySingle[T]($: T)(implicit ev: JsonWriteable[T]) {
    def jsonify: JsValue = ev jsonify $
  }
  implicit class parseJsValue($: JsValue) {
    def parse[T](implicit ev: JsonReadable[T]): T = ev.parse($)
  }
  implicit class parseArray($: JsArray) {
    def parse[T](implicit ev: JsonReadable[T]): Seq[T] = $.value map ev.parse
  }
}
