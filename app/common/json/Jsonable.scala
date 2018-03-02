package common.json

import common.RichJson._
import monocle.Iso
import play.api.libs.json._

import scala.annotation.implicitNotFound

/** Saner names for play's JSON trait, and less optionality. */
@implicitNotFound("Could not prove that ${A} is Jsonable.")
trait Jsonable[A] {
  def jsonify(e: A): JsValue
  def parse(json: JsValue): A
}

object Jsonable {
  implicit def formatJsonable[A](implicit ev: Format[A]): Jsonable[A] = new Jsonable[A] {
    override def jsonify(a: A): JsValue = ev writes a
    override def parse(json: JsValue): A = ev.reads(json).get
  }
  implicit def covariantJsonableJsonable[A](implicit ev: CovariantJsonable[A, _ <: A]): Jsonable[A] =
    new Jsonable[A] with ToJsonableOps {
      override def jsonify(a: A) = ev.jsonify(a)
      override def parse(json: JsValue): A = ev.parse(json)
    }
  implicit def seqJsonable[A: Jsonable]: Jsonable[Seq[A]] = new Jsonable[Seq[A]] with ToJsonableOps {
    override def jsonify(as: Seq[A]): JsValue = JsArray(as.map(_.jsonify))
    override def parse(json: JsValue): Seq[A] = json.as[JsArray].value.map(_.parse[A])
  }
  implicit def optionJsonable[A: Jsonable]: Jsonable[Option[A]] = new Jsonable[Option[A]] with ToJsonableOps {
    override def jsonify(o: Option[A]): JsValue = if (o.isDefined) o.get.jsonify else JsNull
    override def parse(json: JsValue): Option[A] = json match {
      case JsNull => None
      case _ => Some(json.parse[A])
    }
  }
  implicit def pairJsonable[A: Jsonable, B: Jsonable]: Jsonable[(A, B)] = new Jsonable[(A, B)] with ToJsonableOps {
    private val firstKey = "1"
    private val secondKey = "2"
    override def jsonify(t: (A, B)): JsValue =
      Json.obj(firstKey -> t._1.jsonify, secondKey -> t._2.jsonify)
    override def parse(json: JsValue): (A, B) =
      json.value(firstKey).parse[A] -> json.value(secondKey).parse[B]
  }
  def isoJsonable[A, B: Jsonable](aToB: Iso[A, B]): Jsonable[A] = new Jsonable[A] with ToJsonableOps {
    override def parse(json: JsValue): A = aToB.reverseGet(json.parse[B])
    override def jsonify(a: A): JsValue = aToB.get(a).jsonify
  }
}

