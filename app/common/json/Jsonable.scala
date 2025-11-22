package common.json

import play.api.libs.json._

import scala.annotation.implicitNotFound

import common.rich.func.kats.ToMoreFoldableOps.toMoreFoldableOps
import monocle.Iso

import common.json.RichJson._
import common.json.ToJsonableOps._

/** Saner names for play's JSON trait, and less optionality. */
@implicitNotFound("Could not prove that ${A} is Jsonable.")
trait Jsonable[A] {
  def jsonify(e: A): JsValue
  def parse(json: JsValue): A
}

object Jsonable {
  implicit def formatJsonable[A](implicit ev: Format[A]): Jsonable[A] = new Jsonable[A] {
    override def jsonify(a: A): JsValue = ev.writes(a)
    override def parse(json: JsValue): A = ev.reads(json).get
  }
  implicit def seqJsonable[A: Jsonable]: Jsonable[Seq[A]] = new Jsonable[Seq[A]] {
    override def jsonify(as: Seq[A]): JsValue = JsArray(as.map(_.jsonify))
    override def parse(json: JsValue): Seq[A] = json.as[JsArray].map(_.parse[A])
  }
  implicit def optionJsonable[A: Jsonable]: Jsonable[Option[A]] = new Jsonable[Option[A]] {
    override def jsonify(o: Option[A]): JsValue = o.mapHeadOrElse(_.jsonify, JsNull)
    override def parse(json: JsValue): Option[A] = json match {
      case JsNull => None
      case _ => Some(json.parse[A])
    }
  }
  // Tuples are represented as arrays in typescript, so we do the same for interoperability.
  // See: https://www.typescriptlang.org/docs/handbook/2/objects.html#tuple-types
  implicit def pairJsonable[A: Jsonable, B: Jsonable]: Jsonable[(A, B)] = new Jsonable[(A, B)] {
    override def jsonify(t: (A, B)): JsValue =
      Json.arr(t._1.jsonify, t._2.jsonify)
    override def parse(json: JsValue): (A, B) = {
      val seq = json.as[JsArray].value
      seq(0).parse[A] -> seq(1).parse[B]
    }
  }
  def isoJsonable[A, B: Jsonable](aToB: Iso[A, B]): Jsonable[A] = new Jsonable[A] {
    override def parse(json: JsValue): A = aToB.reverseGet(json.parse[B])
    override def jsonify(a: A): JsValue = aToB.get(a).jsonify
  }
}
