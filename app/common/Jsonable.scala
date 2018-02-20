package common

import common.RichJson._
import monocle.Iso
import play.api.libs.json._

import scala.annotation.implicitNotFound

/** Saner names for play's JSON trait, and less optionality. */
@implicitNotFound("Could not prove that ${T} is Jsonable.")
trait Jsonable[T] extends Format[T] {
  def jsonify(t: T): JsValue
  def parse(json: JsValue): T

  override def reads(json: JsValue) =
    try
      JsSuccess(parse(json))
    catch {
      case e: Exception => JsError(e.getMessage)
    }
  override def writes(o: T) = jsonify(o)
}

// For consistency with simulacrum
object Jsonable {
  trait ToJsonableOps {
    implicit class parseString($: String) {
      def parseJsonable[T: Reads]: T = parseJsValue(Json.parse($)).parse[T]
    }
    implicit class jsonifySingle[T]($: T)(implicit ev: Writes[T]) {
      def jsonify: JsValue = ev writes $
    }
    implicit class parseJsValue($: JsValue) {
      def parse[T](implicit ev: Reads[T]): T = ev.reads($).get
    }
    implicit class parseArray($: JsArray) {
      def parse[T](implicit ev: Reads[T]): Seq[T] = $.value.map(ev.reads(_).get)
    }
  }

  implicit def seqJsonable[A: Format]: Jsonable[Seq[A]] = new Jsonable[Seq[A]] with ToJsonableOps {
    override def jsonify(as: Seq[A]): JsValue = JsArray(as.map(_.jsonify))
    override def parse(json: JsValue): Seq[A] = json.as[JsArray].value.map(_.parse[A])
  }
  implicit def optionJsonable[A: Format]: Jsonable[Option[A]] = new Jsonable[Option[A]] with ToJsonableOps {
    override def jsonify(o: Option[A]): JsValue = if (o.isDefined) o.get.jsonify else JsNull
    override def parse(json: JsValue): Option[A] = implicitly[Format[A]].reads(json).asOpt
  }
  implicit def pairJsonable[A: Format, B: Format]: Jsonable[(A, B)] = new Jsonable[(A, B)] with ToJsonableOps {
    private val firstKey = "1"
    private val secondKey = "2"
    override def jsonify(t: (A, B)): JsValue =
      Json.obj(firstKey -> t._1, secondKey -> t._2.jsonify)
    override def parse(json: JsValue): (A, B) =
      json.value(firstKey).parse[A] -> json.value(secondKey).parse[B]
  }
  def isoJsonable[A, B: Format](aToB: Iso[A, B]): Jsonable[A] = new Jsonable[A] with ToJsonableOps {
    override def parse(json: JsValue): A = aToB.reverseGet(json.parse[B])
    override def jsonify(t: A): JsValue = aToB.get(t).jsonify
  }
}

