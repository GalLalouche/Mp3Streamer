package scala.search
import java.util.concurrent.atomic.AtomicInteger

import common.Jsonable
import models.{Album, Artist, Song}
import org.scalatest.mock.MockitoSugar
import common.rich.primitives.RichOption._
import play.api.libs.json.JsObject

import scala.collection.mutable

class FakeModelsJsonable extends MockitoSugar {
  private implicit val counter = new AtomicInteger
  private implicit val parsedModels = mutable.Map[JsObject, Any]()
  private def fakeJsonify(a: Any): JsObject = {
    val $ = mock[JsObject]
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
