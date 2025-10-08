package common.json

import play.api.libs.json.{JsArray, JsValue}

import scala.util.Try

import common.json.RichJson.ImmutableJsonArray
import common.json.ToJsonableOps.parseJsValue

trait JsonReadable[A] {
  def parse(a: JsValue): A
}

object JsonReadable {
  implicit def jsonableReadable[A](implicit ev: Jsonable[A]): JsonReadable[A] = ev.parse
  implicit def seqTryJsonReadable[A: JsonReadable]: JsonReadable[Seq[Try[A]]] =
    _.as[JsArray].map(js => Try(js.parse[A]))
}
