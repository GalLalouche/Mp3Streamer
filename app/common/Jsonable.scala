package common

import common.RichJson._
import play.api.libs.json.{JsArray, JsNumber, JsObject, JsString, JsValue, Json}

import scala.annotation.implicitNotFound

@implicitNotFound("Could not prove that ${T} is Jsonable.")
trait Jsonable[T] {
  def jsonify(t: T): JsValue
  def parse(json: JsValue): T
}

// For consistency with simulacrum
object Jsonable {
  trait ToJsonableOps {
    implicit class parseString($: String) {
      def parseJsonable[T: Jsonable]: T = parseJsValue(Json.parse($)).parse[T]
    }
    implicit class jsonifySingle[T]($: T)(implicit ev: Jsonable[T]) {
      def jsonify: JsValue = ev jsonify $
    }
    implicit class parseJsValue($: JsValue) {
      def parse[T](implicit ev: Jsonable[T]): T = ev parse $
    }
  }

  // Primivites
  implicit object IntJsonable extends Jsonable[Int] {
    override def jsonify(t: Int): JsValue = JsNumber(t)
    override def parse(json: JsValue): Int = json.as[JsNumber].value.toInt
  }
  implicit object StringJsonable extends Jsonable[String] {
    override def jsonify(t: String): JsValue = JsString(t)
    override def parse(json: JsValue): String = json.as[JsString].value
  }

  // Derived instances
  implicit def seqJsonable[A](implicit ev: Jsonable[A]): Jsonable[Seq[A]] = new Jsonable[Seq[A]] {
    override def jsonify(as: Seq[A]): JsValue = JsArray(as.map(ev.jsonify))
    override def parse(json: JsValue): Seq[A] = json.as[JsArray].value.map(ev.parse)
  }
  implicit def optionJsonable[A: Jsonable]: Jsonable[Option[A]] = new Jsonable[Option[A]] with ToJsonableOps {
    override def jsonify(o: Option[A]): JsValue = JsObject(o.map(e => Seq("v" -> e.jsonify)) getOrElse Nil)
    override def parse(json: JsValue): Option[A] = json.\("v").asOpt[JsValue].map(_.parse[A])
  }
  implicit def pairJsonable[A: Jsonable, B: Jsonable]: Jsonable[(A, B)] =
    new Jsonable[(A, B)] with ToJsonableOps {
      private val firstKey = "1"
      private val secondKey = "2"
      override def jsonify(t: (A, B)): JsValue =
        Json.obj(firstKey -> t._1.jsonify, secondKey -> t._2.jsonify)
      override def parse(json: JsValue): (A, B) =
        implicitly[Jsonable[A]].parse(json value firstKey) -> implicitly[Jsonable[B]].parse(json value secondKey)
    }
}

