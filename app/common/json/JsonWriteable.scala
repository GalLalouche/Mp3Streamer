package common.json

import common.rich.primitives.RichEither._
import play.api.libs.json.JsValue

trait JsonWriteable[A] {
  def jsonify(a: A): JsValue
}

object JsonWriteable {
  implicit def jsonableWriteable[A](implicit ev: Jsonable[A]): JsonWriteable[A] = ev.jsonify
  implicit def eitherJsonable[A, B](implicit evA: JsonWriteable[A], evB: JsonWriteable[B]): JsonWriteable[Either[A, B]] =
    _.resolve(evA.jsonify, evB.jsonify)
}
