package common.json

import play.api.libs.json.JsValue

trait JsonReadable[A] {
  def parse(a: JsValue): A
}

object JsonReadable {
  implicit def jsonableReadable[A](implicit ev: Jsonable[A]): JsonReadable[A] = ev.parse
}
