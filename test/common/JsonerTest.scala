package common

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import org.specs2.mutable.Specification
import play.api.libs.json.JsObject
import org.specs2.matcher.JsonMatchers
import java.text.SimpleDateFormat
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.util.Date

/**
  * Add your spec here.
  * You can mock out a whole application including requests, plugins etc.
  * For more information, consult the wiki.
  */
@RunWith(classOf[JUnitRunner])
class JsonerTest extends Specification with JsonMatchers {
	val $ = Json obj ()
	val str = "foo"
	implicit def toTuple(x: Any) = ((str, Jsoner.jsValue(x)))
	"Jsoner" should {
		"handle" in {
			"ints" in {
				($ + 3).toString must /("foo" -> 3)
			}
			"doubles" in {
				($ + 3.0).toString must /(str -> 3.0)
			}
			"longs" in {
				($ + 123456890123L).toString must /(str -> 123456890123L)
			}
			"boolean" in {
				($ + false).toString must /(str -> false)
			}
			"strings" in {
				($ + "bar").toString must /(str -> "bar")
			}
			"seqs" in {
				($ + List(1, 2)).toString must /(str) /#(1) /2
			}
			"recursive seq" in {
				"one step" in {
					Jsoner.jsValue(List(List(1))).toString must /#(0) /1
				}
				"big tree" in {
					Jsoner.jsValue(List(List(1,2), List(List(3,4), List(5,6)))).toString must /#(1) /#(1) /5
				}
			}
			"all others" in {
				val other = new {
					override def toString = "bar"
				}
				($ + other).toString must /(str -> "bar") 
			}
		}
	}
}