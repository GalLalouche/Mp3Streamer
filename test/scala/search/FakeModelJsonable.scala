package scala.search
import java.util.concurrent.atomic.AtomicInteger

import common.Jsonable
import common.rich.primitives.RichOption._
import models.{Album, Artist, Song}
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
  implicit object SongJsonable extends Jsonable[Song] {
    override def jsonify(t: Song) = fakeJsonify(t)
    override def parse(json: JsObject) = getOrThrow[Song](json)
  }
  implicit object AlbumJsonable extends Jsonable[Album] {
    override def jsonify(t: Album) = fakeJsonify(t)
    override def parse(json: JsObject) = getOrThrow[Album](json)
  }
  implicit object ArtistJsonable extends Jsonable[Artist] {
    override def jsonify(t: Artist) = fakeJsonify(t)
    override def parse(json: JsObject) = getOrThrow[Artist](json)
  }
}
