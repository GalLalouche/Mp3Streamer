package common.json

import play.api.libs.json.{JsValue, Writes}

trait JsonWriteable[A] {
  def jsonify(a: A): JsValue
}

object JsonWriteable {
  implicit def writesJsonWritable[A](implicit ev: Writes[A]): JsonWriteable[A] = ev.writes _
}
