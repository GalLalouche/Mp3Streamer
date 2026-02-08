package musicfinder

import backend.recon.Artist
import models.ArtistDir
import play.api.libs.json.{JsArray, JsString, JsValue}

import common.json.Jsonable
import common.json.ToJsonableOps.jsonifyArray
import common.io.avro.Avroable
import common.io.avro.RichAvro.richGenericRecord
import common.path.ref.{DirectoryRef, PathRefFactory}
import org.apache.avro.Schema
import org.apache.avro.SchemaBuilder
import org.apache.avro.generic.GenericData.Record
import org.apache.avro.generic.GenericRecord

// Much faster to parse and jsonify than ArtistDir.
private case class ArtistToDirectory(artist: Artist, dir: DirectoryRef)
private object ArtistToDirectory {
  implicit def ArtistToDirectoryJsonable(implicit
      pathRefFactory: PathRefFactory,
  ): Jsonable[ArtistToDirectory] = new Jsonable[ArtistToDirectory] {
    override def jsonify(e: ArtistToDirectory): JsValue =
      Vector(e.artist.name, e.dir.path).jsonifyArray
    override def parse(json: JsValue): ArtistToDirectory = json match {
      case JsArray(value) =>
        value.toVector match {
          case Vector(JsString(name), JsString(path)) =>
            ArtistToDirectory(Artist(name), pathRefFactory.parseDirPath(path))
        }
    }
  }
  implicit def ArtistToDirectoryAvroable(implicit
      pathRefFactory: PathRefFactory,
  ): Avroable[ArtistToDirectory] = new Avroable[ArtistToDirectory] {
    override val schema: Schema = SchemaBuilder
      .record("ArtistToDirectory")
      .fields()
      .name("artist")
      .`type`()
      .stringType()
      .noDefault()
      .name("path")
      .`type`()
      .stringType()
      .noDefault()
      .endRecord()
    override def toRecord(a: ArtistToDirectory): GenericRecord = {
      val r = new Record(schema)
      r.put("artist", a.artist.name)
      r.put("path", a.dir.path)
      r
    }
    override def fromRecord(r: GenericRecord): ArtistToDirectory = {
      val artist = r.getString("artist")
      val path = r.getString("path")
      ArtistToDirectory(Artist(artist), pathRefFactory.parseDirPath(path))
    }
  }
  def from(artistDir: ArtistDir): ArtistToDirectory =
    ArtistToDirectory(artistDir.toRecon, artistDir.dir)
}
