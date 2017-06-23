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

  implicit class DynamicJson(js: JsValue) {
    private def jsValue(s: String): JsValue = js \ s get
    def has(str: String): Boolean = {
      val $ = js \ str
      false == $.isInstanceOf[JsUndefined] && $.get != JsNull &&
          ($.get.isInstanceOf[JsString] == false || $.as[String].nonEmpty)
    }
    def asObj: JsObject = js.as[JsObject]
    def /(s: String): JsObject = jsValue(s).as[JsObject]
    def str(s: String): String = jsValue(s).as[String]
    def int(s: String): Int = jsValue(s).as[Int]
    def double(s: String): Double = jsValue(s).as[Double]
    def ostr(s: String): Option[String] = js.\(s).asOpt[String]
    def array(s: String): JsArray = jsValue(s).as[JsArray]
    def objects(s: String): Seq[JsObject] = jsValue(s).as[JsArray].value.map(_.as[JsObject])
  }
}
