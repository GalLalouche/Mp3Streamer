package backend.search.cache

import models.Song
import play.api.libs.json.{JsArray, JsValue}

import scala.math.Ordering.Implicits._

import common.AvroableSaver
import common.io.avro.Avroable
import common.json.Jsonable
import common.json.RichJson.ImmutableJsonArray
import common.json.ToJsonableOps._
import common.path.ref.FileRef
import common.rich.RichT.richT
import common.rich.RichTime._
import common.rich.collections.RichTraversableOnce.richTraversableOnce

class SongCache private (private val songsByFile: Map[FileRef, TimestampedSong]) {
  def songs: Iterable[Song] = songsByFile.values.map(_.song)
  def needsUpdate(f: FileRef): Boolean =
    songsByFile.get(f).forall(_.updateTime <= f.lastModifiedTime)
  def get: FileRef => Option[TimestampedSong] = songsByFile.get
  def getDeleted(newCache: SongCache): Iterable[TimestampedSong] =
    songsByFile.filterNot(newCache.songsByFile contains _._1).values

  override def equals(other: Any): Boolean =
    other.safeCast[SongCache].exists(_.songsByFile == songsByFile)
  override def hashCode: Int = songsByFile.hashCode
  def save(saver: AvroableSaver)(implicit ev: Avroable[TimestampedSong]): Unit =
    saver.saveExplicit(songsByFile.values, "SongCaches.avro")
}

object SongCache {
  def from(xs: Seq[TimestampedSong]): SongCache = {
    if (xs.isEmpty)
      scribe.warn("No existing SongCache found")
    new SongCache(xs.mapBy(_.song.file))
  }

  implicit def jsonableEv(implicit ev: Jsonable[Song]): Jsonable[SongCache] =
    new Jsonable[SongCache] {
      override def jsonify(e: SongCache): JsValue = e.songsByFile.values.toVector.jsonifyArray
      override def parse(json: JsValue): SongCache =
        from(json.as[JsArray].map(_.parse[TimestampedSong]))
    }

  // TODO this wasn't actually tested! Or rather, it was tested only when empty? The bug here was
  //  the difference in names between "SongCaches" and "TimestampedSongs", which would cause Avro to
  //  load and empty sequence. Maybe the warning above should be pushed down to the saver?
  def load(save: AvroableSaver)(implicit ev: Avroable[TimestampedSong]): SongCache =
    from(save.load[TimestampedSong]("SongCaches.avro"))
}
