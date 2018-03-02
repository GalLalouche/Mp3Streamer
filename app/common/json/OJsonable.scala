package common.json

import play.api.libs.json._

import scala.annotation.implicitNotFound

/** Saner names for play's JSON trait, and less optionality. */
@implicitNotFound("Could not prove that ${T} is OJsonable.")
trait OJsonable[T] extends Jsonable[T] {
  override def jsonify(a: T): JsObject
  override def parse(json: JsValue): T = parse(json.asInstanceOf[JsObject])
  def parse(json: JsObject): T
}

object OJsonable {
  implicit def oFormatOJsonable[A](implicit ev: OFormat[A]): OJsonable[A] = new OJsonable[A] {
    override def jsonify(a: A): JsObject = ev writes a
    override def parse(json: JsObject): A = ev.reads(json).get
  }
}



