package common

import play.api.libs.json._

/** Because play's default json library is stupid and doesn't handle type Any */
object Jsoner {
  implicit def jsValue(x: Any): JsValue = x match {
    case o: String => JsString(o)
    case o: Int => JsNumber(o)
    case o: Long => JsNumber(o)
    case o: Double => JsNumber(o)
    case o: Boolean => JsBoolean(o)
    case o: Seq[_] => JsArray(o.map(jsValue).toVector)
    case o: JsValue => o
    case o: AnyRef => jsValue(o.toString)
  }

  // implicit casts from a JsValue to a primitive
  implicit def jsValueToInt(js: JsValue): Int = js.as[Int]
  implicit def jsValueToSeq(js: JsValue): Seq[JsValue] = js.as[JsArray].value
  implicit def jsValueToJsObject(js: JsValue): JsObject = js.as[JsObject]
  implicit def jsValueToLong(js: JsValue): Long = js.as[Long]
  implicit def jsValueToDouble(js: JsValue): Double = js.as[Double]
  implicit def jsValueToBoolean(js: JsValue): Boolean = js.as[Boolean]
  implicit def jsValueToString(js: JsValue): String = js.as[String]
}
