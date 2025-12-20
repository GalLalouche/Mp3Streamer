package backend.last_albums

import java.time.LocalDateTime

import models.AlbumDir
import play.api.libs.json.JsValue

import scala.collection.immutable.Queue
import scala.math.Ordering.Implicits.infixOrderingOps

import common.rich.func.TuplePLenses.__2

import common.json.Jsonable
import common.json.ToJsonableOps.{jsonifySingle, parseJsValue}

private class LastAlbums private (
    private val queue: Queue[AlbumDir],
    val lastUpdateTime: LocalDateTime,
) {
  def this(now: LocalDateTime) = this(Queue.empty, now)
  def enqueue(albumDir: AlbumDir): LastAlbums = {
    val modified = albumDir.dir.lastModified
    if (modified <= lastUpdateTime) this else new LastAlbums(queue.enqueue(albumDir), modified)
  }
  def enqueueAll(albumDirs: Seq[AlbumDir]): LastAlbums =
    albumDirs.sortBy(_.dir.lastModified).foldLeft(this)(_.enqueue(_))
  def dequeue: Option[(AlbumDir, LastAlbums)] =
    queue.dequeueOption.map(__2.modify(new LastAlbums(_, lastUpdateTime)))
  def albums: Seq[AlbumDir] = queue
}

private object LastAlbums {
  implicit def jsonableLastAlbums(implicit ev: Jsonable[AlbumDir]): Jsonable[LastAlbums] =
    new Jsonable[LastAlbums] {
      override def jsonify(e: LastAlbums): JsValue =
        (e.queue: Seq[AlbumDir], e.lastUpdateTime).jsonify
      override def parse(json: JsValue): LastAlbums = {
        val (dirs, lastUpdateTime) = json.parse[(Seq[AlbumDir], LocalDateTime)]
        new LastAlbums(Queue(dirs: _*), lastUpdateTime)
      }
    }
}
