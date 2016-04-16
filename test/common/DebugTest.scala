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
///**
//  * Add your spec here.
//  * You can mock out a whole application including requests, plugins etc.
//  * For more information, consult the wiki.
//  */
//@RunWith(classOf[JUnitRunner])
//class DebugTest extends Specification with Debug {
//	val $ = Json obj ()
//	val str = "foo"
//	implicit def toTuple(x: Any) = ((str, Jsoner.jsValue(x)))
//
//	"TrySleep" should {
//		"throw the last exception when" >> {
//			"always failing" >> {
//				val e = new Exception;
//				{ trySleep(maxTries = 2, 1) { throw e; () } } should throwAn(e);
//			}
//			"short one try" >> {
//				var i = 0;
//				var tries = 3;
//				val e = new Exception;
//				{
//					trySleep(maxTries = tries - 1, 1) {
//						if (i < tries) {
//							i += 1; throw new Exception;
//						}
//						0;
//					}
//				} should throwAn(e);
//			}
//		}
//		"return value returned by block" >> {
//			"when has no exceptions" >> {
//				trySleep(1, 1) { 42 } === 42
//			}
//			"when has less exceptions than max" >>
//				{
//					var first = true;
//					trySleep(1, 1) {
//						if (first) {
//							first = false;
//							throw new Exception;
//						}
//						42
//					} === 42
//				}
//		}
//	}
//}
