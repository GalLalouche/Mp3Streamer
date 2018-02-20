package common.json

import common.RichJson._
import monocle.Iso
import play.api.libs.json._

import scala.annotation.implicitNotFound

/** Saner names for play's JSON trait, and less optionality. */
@implicitNotFound("Could not prove that ${T} is Jsonable.")
trait Jsonable[T] extends Format[T] with JsonWriteable[T] with JsonReadable[T] {
  override def jsonify(t: T): JsValue
  override def parse(json: JsValue): T

  override def reads(json: JsValue) =
    try
      JsSuccess(parse(json))
    catch {
      case e: Exception => JsError(e.getMessage)
    }
  override def writes(o: T) = jsonify(o)
}

object Jsonable {
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
      Json.obj(firstKey -> t._1, secondKey -> t._2.jsonify)
    override def parse(json: JsValue): (A, B) =
      json.value(firstKey).parse[A] -> json.value(secondKey).parse[B]
  }
  def isoJsonable[A, B: Jsonable](aToB: Iso[A, B]): Jsonable[A] = new Jsonable[A] with ToJsonableOps {
    override def parse(json: JsValue): A = aToB.reverseGet(json.parse[B])
    override def jsonify(t: A): JsValue = aToB.get(t).jsonify
  }
}

