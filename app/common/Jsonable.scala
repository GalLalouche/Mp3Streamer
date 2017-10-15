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
    implicit class parseString($: String) {
      def toJsonObj: JsObject = Json.parse($).as[JsObject]
    }
    implicit class jsonifySingle[T]($: T)(implicit ev: Jsonable[T]) {
      def jsonify: JsObject = ev jsonify $
    }
    implicit class parseObject($: JsObject) {
      def parse[T](implicit ev: Jsonable[T]): T = ev parse $
    }
  }

  // Primivites
  implicit object IntJsonable extends Jsonable[Int] {
    override def jsonify(t: Int): JsObject = Json.obj("v" -> t)
    override def parse(json: JsObject): Int = json int "v"
  }
  implicit object StringJsonable extends Jsonable[String] {
    override def jsonify(t: String): JsObject = Json.obj("v" -> t)
    override def parse(json: JsObject): String = json str "v"
  }

  // Derived instances
  implicit def seqJsonable[A](implicit ev: Jsonable[A]): Jsonable[Seq[A]] = new Jsonable[Seq[A]] {
    override def parse(json: JsObject): Seq[A] = json.objects("v").map(ev.parse)
    override def jsonify(as: Seq[A]): JsObject = Json.obj("v" -> JsArray(as.map(ev.jsonify)))
  }
  implicit def optionJsonable[A: Jsonable]: Jsonable[Option[A]] = new Jsonable[Option[A]] with ToJsonableOps {
    override def parse(json: JsObject): Option[A] = json.\("v").asOpt[JsObject].map(_.parse[A])
    override def jsonify(o: Option[A]): JsObject = JsObject(o.map(e => Seq("v" -> e.jsonify)) getOrElse Nil)
  }
  implicit def pairJsonable[A: Jsonable, B: Jsonable]: Jsonable[(A, B)] =
    new Jsonable[(A, B)] with ToJsonableOps {
      private val firstKey = "1"
      private val secondKey = "2"
      override def jsonify(t: (A, B)): JsObject =
        Json.obj(firstKey -> t._1.jsonify, secondKey -> t._2.jsonify)
      override def parse(json: JsObject): (A, B) =
        implicitly[Jsonable[A]].parse(json / firstKey) -> implicitly[Jsonable[B]].parse(json / secondKey)
    }
}

