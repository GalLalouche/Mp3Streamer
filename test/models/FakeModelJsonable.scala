package models

import java.util.concurrent.atomic.AtomicInteger

import play.api.libs.json.{JsObject, Json, JsValue}

import scala.collection.mutable

import common.json.Jsonable
import common.rich.primitives.RichOption._

class FakeModelJsonable {
  private val counter = new AtomicInteger
  private val parsedModels = mutable.Map[JsValue, Any]()
  private def fakeJsonify(a: Any): JsObject = {
    val $ = Json.obj("index" -> counter.getAndIncrement())
    parsedModels += $ -> a
    $
  }
  private def getOrThrow[T](json: JsValue): T =
    parsedModels
      .get(json)
      .getOrThrow("Tried to load a JSON that wasn't returned from an instance of this class")
      .asInstanceOf[T]
  // Requiring T <: AnyRef prevents infinite recursions with primitives
  implicit def FakeJsonable[A <: AnyRef]: Jsonable[A] = new Jsonable[A] {
    override def jsonify(a: A) = fakeJsonify(a)
    override def parse(json: JsValue) = getOrThrow[A](json)
  }
}
