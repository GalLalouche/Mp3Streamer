package common

import play.api.libs.json._

object RichJson {
  implicit class DynamicJson(js: JsValue) {
    def value(str: String): JsValue = js.\(str).get
    def has(str: String): Boolean = js.\(str).toOption.exists(e => e != JsNull && e.asOpt[String].forall(_.nonEmpty))
    def /(s: String): JsObject = value(s).as[JsObject]
    def str(s: String): String = value(s).as[String]
    def int(s: String): Int = value(s).as[Int]
    def double(s: String): Double = value(s).as[Double]
    def ostr(s: String): Option[String] = js.\(s).asOpt[String]
    def array(s: String): JsArray = value(s).as[JsArray]
    def objects(s: String): Seq[JsObject] = value(s).as[JsArray].value.map(_.as[JsObject])
  }
}
