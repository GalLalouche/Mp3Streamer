package common

import play.api.libs.json._

import scalaz.std.option.optionInstance
import common.rich.func.ToMoreFoldableOps._

import common.json.JsonWriteable
import common.json.ToJsonableOps._

object RichJson {
  implicit class DynamicJson(private val $: JsValue) extends AnyVal {
    def value(str: String): JsValue = $.\(str).get
    def has(str: String): Boolean =
      $.\(str).toOption.exists(e => e != JsNull && e.asOpt[String].forall(_.nonEmpty))
    def /(s: String): JsObject = value(s).as[JsObject]
    def str(s: String): String = value(s).as[String]
    def int(s: String): Int = value(s).as[Int]
    def double(s: String): Double = value(s).as[Double]
    def ostr(s: String): Option[String] = $.\(s).asOpt[String]
    def array(s: String): JsArray = value(s).as[JsArray]
    def objects(s: String): Seq[JsObject] = value(s).as[JsArray].value.map(_.as[JsObject])
  }

  implicit class DynamicJsonObject(private val $: JsObject) extends AnyVal {

    def append[A: JsonWriteable](e: (String, Option[A])): JsObject =
      e._2.map(_.jsonify).map(e._1.->).mapHeadOrElse($.+, $)
  }
}
