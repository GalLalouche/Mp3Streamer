package backend.mb

import org.scalatest.{FreeSpec, ShouldMatchers}
import play.api.libs.json.{JsNull, Json}
import JsonHelper._

class JsonHelperTest extends FreeSpec with ShouldMatchers {
  
  
  "RichJson" - {
    "has" - {
      "valid string" in {
        Json.obj("foo" -> "bar").has("foo") should be === true
      }
      "valid int" in {
        Json.obj("foo" -> 4) has ("foo") should be === true
      }
      "no such key" in {
        Json.obj("foo" -> "bar").has("baz") should be === false
      }
      "null" in {
        Json.obj("foo" -> JsNull).has("foo") should be === false
      }
      "empty string" in {
        Json.obj("foo" -> "").has("foo") should be === false
      }
    }
  }
}
