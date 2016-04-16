//package common
//
//import org.junit.runner.RunWith
//import org.specs2.runner.JUnitRunner
//import play.api.libs.json.Json
//import play.api.libs.json.Json.toJsFieldJsValueWrapper
//import org.specs2.mutable.Specification
//import play.api.libs.json.JsObject
//import org.specs2.matcher.JsonMatchers
//import java.text.SimpleDateFormat
//import org.joda.time.DateTime
//import org.joda.time.format.DateTimeFormat
//import java.util.Date
//
//@RunWith(classOf[JUnitRunner])
//class JsonerTest extends Specification with JsonMatchers {
//  val $ = Json obj()
//  val str = "foo"
//  implicit def toTuple(x: Any) = ((str, Jsoner.jsValue(x)))
//  "Jsoner" should {
//    "handle" in {
//      "ints" in {
//        ($ + 3).toString must /("foo" -> 3)
//      }
//      "doubles" in {
//        ($ + 3.0).toString must /(str -> 3.0)
//      }
//      "longs" in {
//        ($ + 123456890123L).toString must /(str -> 123456890123L)
//      }
//      "boolean" in {
//        ($ + false).toString must /(str -> false)
//      }
//      "strings" in {
//        ($ + "bar").toString must /(str -> "bar")
//      }
//      "seqs" in {
//        val json = ($ + List(1, 2)).\(str)
//        json(0).as[Int] mustEqual 1
//        json(1).as[Int] mustEqual 2
//      }
//      "all others" in {
//        val other = new {
//          override def toString = "bar"
//        }
//        ($ + other).toString must /(str -> "bar")
//      }
//    }
//  }
//}
