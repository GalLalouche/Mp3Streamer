package models

import java.util.concurrent.atomic.AtomicInteger

import common.Jsonable
import common.rich.primitives.RichOption._
import play.api.libs.json.{JsObject, Json}

import scala.collection.mutable

class FakeModelJsonable {
  private implicit val counter = new AtomicInteger
  private implicit val parsedModels = mutable.Map[JsObject, Any]()
  private def fakeJsonify(a: Any): JsObject = {
    val $ = Json.obj("index" -> counter.getAndIncrement())
    parsedModels += $ -> a
    $
  }
  private def getOrThrow[T](json: JsObject): T =
    parsedModels.get(json)
        .getOrThrow(s"Tried to load a JSON that wasn't returned from an instance of this class")
        .asInstanceOf[T]
  implicit def FakeJsonable[T]: Jsonable[T] = new Jsonable[T] {
    override def jsonify(t: T) = fakeJsonify(t)
    override def parse(json: JsObject) = getOrThrow[T](json)
  }
}
