package common

import play.api.libs.json._

object RichJson {
  implicit def toJsValue(x: Any): JsValue = x match {
    case o: String => JsString(o)
    case o: Int => JsNumber(o)
    case o: Long => JsNumber(o)
    case o: Double => JsNumber(o)
    case o: Boolean => JsBoolean(o)
    case o: Seq[_] => JsArray(o.map(toJsValue))
    case o: JsValue => o
    case o: AnyRef => toJsValue(o.toString)
  }

  // implicit casts from a JsValue to a primitive
  implicit def jsValueToInt(js: JsValue): Int = js.as[Int]
  implicit def jsValueToSeq(js: JsValue): Seq[JsValue] = js.as[JsArray].value
  implicit def jsValueToJsObject(js: JsValue): JsObject = js.as[JsObject]
  implicit def jsValueToLong(js: JsValue): Long = js.as[Long]
  implicit def jsValueToDouble(js: JsValue): Double = js.as[Double]
  implicit def jsValueToBoolean(js: JsValue): Boolean = js.as[Boolean]
  implicit def jsValueToString(js: JsValue): String = js.as[String]

  implicit class DynamicJson(js: JsValue) {
    def has(str: String): Boolean = {
      val $ = js \ str
      false == $.isInstanceOf[JsUndefined] && $.get != JsNull &&
          ($.get.isInstanceOf[JsString] == false || $.as[String].nonEmpty)
    }
    def /(s: String): JsValue = js \ s get
    def str(s: String): String = /(s)
    def ostr(s: String): Option[String] = js.\(s).asOpt[String]
    def array(s: String): Seq[JsValue] = /(s)
  }
}
