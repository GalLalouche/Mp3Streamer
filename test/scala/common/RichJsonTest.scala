package scala.common

import common.AuxSpecs
import common.RichJson._
import common.rich.RichT._
import org.scalatest.FreeSpec
import play.api.libs.json.{JsNull, JsObject, JsValue}

class RichJsonTest extends FreeSpec with AuxSpecs {
  def withObject(a: Any) = JsObject(Map("foo" -> toJsValue(a)))
  "to primitive" - {
    def get(js: JsObject): JsValue = js.\("foo").get
    "int" in {
      get(withObject(1)) shouldReturn 1
    }
    "seq" in {
      get(withObject(List(1, 2))) shouldReturn List(1, 2)
    }
    "jsObject" in {
      get(get(withObject(withObject(1)))) shouldReturn 1
    }
    "double" in {
      get(withObject(2.5)) shouldReturn 2.5
    }
    "boolean" in {
      get(withObject(true)) shouldReturn true
    }
    "string" in {
      get(withObject("bar")) shouldReturn "bar"
    }
  }
  "dynamic json" - {
    "exists" in {
      withObject(1) / "foo" shouldReturn 1
    }
    "if does not exist should throw an exception" in {
      a[NoSuchElementException] should be thrownBy withObject(1)./("bar")
    }
  }
  "has" - {
    def has(js: JsObject) = js has "foo" shouldReturn true
    def hasNot(js: JsObject) = js has "foo" shouldReturn false
    "valid string" in {
      withObject("bar") |> has
    }
    "valid int" in {
      withObject(4) |> has
    }
    "no such key" in {
      withObject("bar") |> has
    }
    "null" in {
      withObject(JsNull) |> hasNot
    }
    "empty string" in {
      withObject("") |> hasNot
    }
  }
}
