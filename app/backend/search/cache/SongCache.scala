package backend.search.cache

import models.Song
import play.api.libs.json.{JsArray, JsValue}

import scala.math.Ordering.Implicits._

import common.io.FileRef
import common.json.Jsonable
import common.json.RichJson.ImmutableJsonArray
import common.json.ToJsonableOps._
import common.rich.RichT.richT
import common.rich.RichTime._
import common.rich.collections.RichTraversableOnce.richTraversableOnce

private class SongCache private (private val songsByFile: Map[FileRef, TimestampedSong]) {
  def songs: Iterable[Song] = songsByFile.values.map(_.song)
  def needsUpdate(f: FileRef): Boolean = songsByFile.get(f).forall(_.updateTime <= f.lastModified)
  def get: FileRef => Option[TimestampedSong] = songsByFile.get
  def getDeleted(newCache: SongCache): Iterable[TimestampedSong] =
    songsByFile.filterNot(newCache.songsByFile contains _._1).values

  override def equals(other: Any): Boolean =
    other.safeCast[SongCache].exists(_.songsByFile == songsByFile)
  override def hashCode: Int = songsByFile.hashCode
}

private object SongCache {
  def from(xs: Seq[TimestampedSong]): SongCache = new SongCache(xs.mapBy(_.song.file))

  implicit def jsonableEv(implicit ev: Jsonable[Song]): Jsonable[SongCache] =
    new Jsonable[SongCache] {
      override def jsonify(e: SongCache): JsValue = e.songsByFile.values.toVector.jsonifyArray
      override def parse(json: JsValue): SongCache =
        from(json.as[JsArray].map(_.parse[TimestampedSong]))
    }
}
