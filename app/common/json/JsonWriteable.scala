package common.json

import play.api.libs.json.{JsValue, Writes}
import common.rich.primitives.RichEither._

trait JsonWriteable[A] {
  def jsonify(a: A): JsValue
}

object JsonWriteable {
  implicit def writesJsonWritable[A](implicit ev: Writes[A]): JsonWriteable[A] = ev.writes _
  implicit def jsonableWriteable[A](implicit ev: Jsonable[A]): JsonWriteable[A] = ev.jsonify
  implicit def eitherJsonable[A, B](implicit evA: JsonWriteable[A], evB: JsonWriteable[B]): JsonWriteable[Either[A, B]] =
    _.resolve(evA.jsonify, evB.jsonify)
}
