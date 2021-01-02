package backend.search.cache

import models.Song
import play.api.libs.json.{JsArray, JsValue}

import scala.math.Ordering.Implicits._

import common.io.{FileRef, JsonableSaver}
import common.json.Jsonable
import common.json.ToJsonableOps._
import common.rich.RichTime._
import common.rich.collections.RichTraversableOnce.richTraversableOnce
import common.rich.primitives.RichOption.richOption

// case class for free equals and hashCode.
private case class SongCache private(private val songsByFile: Map[FileRef, TimestampedSong]) {
  def songs: Iterable[Song] = songsByFile.values.map(_.song)
  def needsUpdate(f: FileRef): Boolean = songsByFile.get(f).forall(_.updateTime <= f.lastModified)
  def get: FileRef => Option[TimestampedSong] = songsByFile.get
  def getDeleted(newCache: SongCache): Iterable[TimestampedSong] =
    songsByFile.filterNot(newCache.songsByFile contains _._1).values
}

private object SongCache {
  def apply(xs: Seq[TimestampedSong]): SongCache = new SongCache(xs.mapBy(_.file))
  def fromSongJsonFile(jsonableSaver: JsonableSaver)(implicit ev: Jsonable[Song]): SongCache = {
    val time = jsonableSaver.lastUpdateTime[Song] getOrThrow "Could not find any song JSON file"
    val songs = jsonableSaver.loadArray[Song]
    SongCache(songs.map(TimestampedSong(time, _)))
  }
  implicit def jsonableEv(implicit ev: Jsonable[Song]): Jsonable[SongCache] = new Jsonable[SongCache] {
    override def jsonify(e: SongCache): JsValue = e.songsByFile.values.toVector.jsonifyArray
    override def parse(json: JsValue): SongCache = apply(json.as[JsArray].value.map(_.parse[TimestampedSong]))
  }
}
