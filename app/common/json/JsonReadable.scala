package common.json

import play.api.libs.json.{JsValue, Reads}

trait JsonReadable[A] {
  def parse(a: JsValue): A
}

object JsonReadable {
  implicit def readsJsonReadable[A](implicit ev: Reads[A]): JsonReadable[A] = ev.reads(_).get
  implicit def jsonableReadable[A](implicit ev: Jsonable[A]): JsonReadable[A] = ev.parse
}

