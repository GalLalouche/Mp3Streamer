package common

import play.api.libs.json._

object RichJson {
  implicit class DynamicJson(js: JsValue) {
    private def jsValue(s: String): JsValue = js.\(s).get
    def has(str: String): Boolean = js.\(str).toOption.exists(e => e != JsNull && e.asOpt[String].forall(_.nonEmpty))
    def /(s: String): JsObject = jsValue(s).as[JsObject]
    def str(s: String): String = jsValue(s).as[String]
    def int(s: String): Int = jsValue(s).as[Int]
    def double(s: String): Double = jsValue(s).as[Double]
    def ostr(s: String): Option[String] = js.\(s).asOpt[String]
    def array(s: String): JsArray = jsValue(s).as[JsArray]
    def objects(s: String): Seq[JsObject] = jsValue(s).as[JsArray].value.map(_.as[JsObject])
  }
}
