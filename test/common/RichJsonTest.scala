package common

import org.scalatest.{FreeSpec, Matchers}
import play.api.libs.json.{JsNull, JsNumber, JsObject, Json}
import play.api.libs.json.Json.JsValueWrapper

import common.json.RichJson._
import common.rich.RichT._
import common.test.AuxSpecs

class RichJsonTest extends FreeSpec with AuxSpecs with Matchers {
  private def withObject(a: JsValueWrapper): JsObject = Json.obj("foo" -> a)
  "dynamic json" - {
    "exists" in {
      withObject(1).int("foo") shouldReturn 1
    }
    "if does not exist should throw an exception" in {
      a[NoSuchElementException] should be thrownBy withObject(1).str("bar")
    }
  }
  "has" - {
    def verifyHas(js: JsObject): Unit = js.has("foo") shouldReturn true
    def verifyHasNot(js: JsObject): Unit = js.has("foo") shouldReturn false
    "valid string" in {
      withObject("bar") |> verifyHas
    }
    "valid int" in {
      withObject(4) |> verifyHas
    }
    "no such key" in {
      withObject("bar") |> verifyHas
    }
    "null" in {
      withObject(JsNull) |> verifyHasNot
    }
    "empty string" in {
      withObject("") |> verifyHasNot
    }
  }

  "DynamicJsonObject" - {
    val $ = Json.obj("foo" -> 2)
    "append" - {
      "None" in {
        $.append[Int]("bar" -> None) shouldReturn $
      }
      "Some" in {
        $.append("bar" -> Some(4)) shouldReturn $ + ("bar" -> JsNumber(4))
      }
    }
  }
}
