package backend.id3

import common.RichJson._
import common.json.Jsonable
import play.api.libs.json.{JsValue, Json}

case class Id3Metadata(
    title: String,
    artistName: String,
    albumName: String,
    track: Int,
    year: Int,
    discNumber: String)

object Id3Metadata {
  implicit object Id3MetadataJsonable extends Jsonable[Id3Metadata] {
    override def jsonify(t: Id3Metadata): JsValue = Json.obj(
      "title" -> t.title,
      "artistName" -> t.artistName,
      "albumName" -> t.albumName,
      "track" -> t.track,
      "year" -> t.year,
      "discNumber" -> t.discNumber)
    override def parse(json: JsValue): Id3Metadata = Id3Metadata(
      title = json str "title",
      artistName = json str "artistName",
      albumName = json str "albumName",
      track = json int "track",
      year = json int "year",
      discNumber = json ostr "discNumber" getOrElse "")
  }
}
