package common

import play.api.libs.json.{JsArray, JsObject}


trait Jsonable[T] {
  def jsonify(t: T): JsObject
  def jsonify(ts: Seq[T]): JsArray = JsArray(ts map jsonify)
  def parse(json: JsObject): T
  def parse(json: JsArray): Seq[T] = json.value map (_.asInstanceOf[JsObject]) map parse
}

// For consistency with simulacrum
object Jsonable {
  trait ToJsonableOps {
    implicit class jsonifySingle[T]($: T)(implicit ev: Jsonable[T]) {
      def jsonify: JsObject = ev jsonify $
    }
    implicit class jsonifySequence[T]($: Seq[T])(implicit ev: Jsonable[T]) {
      def jsonify: JsArray = ev jsonify $
    }
    implicit class parseObject[T]($: JsObject)(implicit ev: Jsonable[T]) {
      def parse: T = ev parse $
    }
    implicit class parseArray[T]($: JsArray)(implicit ev: Jsonable[T]) {
      def parse: Seq[T] = ev parse $
    }
  }
}

