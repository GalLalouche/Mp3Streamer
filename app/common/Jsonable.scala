package common

import common.RichJson._
import play.api.libs.json.{JsArray, JsObject, Json}

trait Jsonable[T] {
  def jsonify(t: T): JsObject
  def parse(json: JsObject): T
}

// For consistency with simulacrum
object Jsonable {
  trait ToJsonableOps {
    implicit class jsonifySingle[T]($: T)(implicit ev: Jsonable[T]) {
      def jsonify: JsObject = ev jsonify $
    }
    implicit class parseObject($: JsObject) {
      def parse[T](implicit ev: Jsonable[T]): T = ev parse $
    }
  }

  // Derived instances
  implicit def seqJsonable[A](implicit ev: Jsonable[A]): Jsonable[Seq[A]] = new Jsonable[Seq[A]] {
    override def parse(json: JsObject): Seq[A] = json.objects("values").map(ev.parse)
    override def jsonify(as: Seq[A]): JsObject = Json.obj("values" -> JsArray(as.map(ev.jsonify)))
  }
}

