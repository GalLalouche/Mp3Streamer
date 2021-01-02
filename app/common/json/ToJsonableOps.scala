package common.json

import play.api.libs.json._

trait ToJsonableOps {
  implicit class jsonifyString($: String) {
    /** Assumes the string is a valid JSON string. A raw unquoted string is *not* a valid JSON. */
    def parseJsonable[T: JsonReadable]: T = Json.parse($).parse[T]
  }
  implicit class jsonifySingle[T]($: T)(implicit ev: JsonWriteable[T]) {
    def jsonify: JsValue = ev jsonify $
  }
  implicit class jsonifyArray[T]($: Seq[T])(implicit ev: JsonWriteable[T]) {
    def jsonifyArray: JsArray = JsArray($.map(_.jsonify))
  }
  implicit class parseJsValue($: JsValue) {
    def parse[T](implicit ev: JsonReadable[T]): T = ev.parse($)
  }
  implicit class parseArray($: JsArray) {
    def parse[T](implicit ev: JsonReadable[T]): Seq[T] = $.value map ev.parse
  }
}

object ToJsonableOps extends ToJsonableOps
