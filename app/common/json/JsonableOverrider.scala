package common.json

import play.api.libs.json.{JsObject, JsValue}

trait JsonableOverrider[A] {
  def jsonify(a: A, original: => JsValue): JsValue = original
  def parse(json: JsValue, original: => A): A = original
}
trait OJsonableOverrider[A] {
  def jsonify(a: A, original: => JsObject): JsObject = original
  def parse(json: JsObject, original: => A): A = original
}
object JsonableOverrider {
  def apply[A](overrider: JsonableOverrider[A])(implicit ev: Jsonable[A]): Jsonable[A] =
    new Jsonable[A] {
      override def jsonify(a: A): JsValue = overrider.jsonify(a, ev.jsonify(a))
      override def parse(json: JsValue): A = overrider.parse(json, ev.parse(json))
    }
  def apply[A](overrider: OJsonableOverrider[A])(implicit ev: OJsonable[A]): OJsonable[A] =
    new OJsonable[A] {
      override def jsonify(a: A): JsObject = overrider.jsonify(a, ev.jsonify(a))
      override def parse(json: JsObject): A = overrider.parse(json, ev.parse(json))
    }
  def jsonify[A: Jsonable](f: (A, => JsValue) => JsValue): Jsonable[A] =
    apply[A](new JsonableOverrider[A] {
      override def jsonify(a: A, original: => JsValue) = f(a, original)
    })
  def parse[A: Jsonable](f: (JsValue, => A) => A): Jsonable[A] = apply[A](new JsonableOverrider[A] {
    override def parse(json: JsValue, original: => A) = f(json, original)
  })
  def oJsonify[A: OJsonable](f: (A, => JsObject) => JsObject): OJsonable[A] =
    apply[A](new OJsonableOverrider[A] {
      override def jsonify(a: A, original: => JsObject) = f(a, original)
    })
  def oParse[A: OJsonable](f: (JsObject, => A) => A): OJsonable[A] =
    apply[A](new OJsonableOverrider[A] {
      override def parse(json: JsObject, original: => A) = f(json, original)
    })
}
