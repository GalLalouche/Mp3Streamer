package common

import play.api.libs.json.{JsArray, JsObject}


trait Jsonable[T] {
  def jsonify(t: T): JsObject
  def jsonify(ts: Seq[T]): JsArray = JsArray(ts map jsonify)
  def parse(json: JsObject): T
  def parse(json: JsArray): Seq[T] = json.value map (_.as[JsObject]) map parse
}

