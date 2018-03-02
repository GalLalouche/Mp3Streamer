package common.json

import play.api.libs.json.JsValue

trait JsonableOverrider[A] {
  def jsonify(t: A, original: => JsValue): JsValue = original
  def parse(json: JsValue, original: => A): A = original
}
object JsonableOverrider {
  def apply[A](overrider: JsonableOverrider[A])(implicit ev: Jsonable[A]): Jsonable[A] = new Jsonable[A] {
    override def jsonify(t: A): JsValue = overrider.jsonify(t, ev jsonify t)
    override def parse(json: JsValue): A = overrider.parse(json, ev parse json)
  }
  def jsonify[A: Jsonable](f: (A, => JsValue) => JsValue): Jsonable[A] = apply[A](new JsonableOverrider[A] {
    override def jsonify(t: A, original: => JsValue) = f(t, original)
  })
  def parse[A: Jsonable](f: (JsValue, => A) => A): Jsonable[A] = apply[A](new JsonableOverrider[A] {
    override def parse(json: JsValue, original: => A) = f(json, original)
  })
}
