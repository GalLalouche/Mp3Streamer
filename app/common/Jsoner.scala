package common

import play.api.libs.json._
import play.api.libs.json.Json.JsValueWrapper

/**
 * Because play's default json library is stupid and doesn't handle type Any
 */
object Jsoner {
	implicit def jsValue(x: Any): JsValue = x match {
		case o: String => JsString(o)
		case o: Int => JsNumber(o)
		case o: Long => JsNumber(o)
		case o: Double => JsNumber(o)
		case o: Boolean => JsBoolean(o)
		case o: Seq[_] => JsArray(o.map(jsValue))
		case _ => JsString(x.toString)
	}
	
	implicit def jsValue(x: (String, _ <: Any)): (String, JsValueWrapper) = ((x._1, jsValue(x._2)))
	implicit object Impliciter extends Writes[Any] {
		def writes(x: Any) = {
			jsValue(x)
		}
	}
}