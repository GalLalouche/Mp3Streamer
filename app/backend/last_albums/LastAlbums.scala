package backend.last_albums

import java.time.LocalDateTime

import models.AlbumDir
import play.api.libs.json.JsValue

import scala.collection.immutable.Queue
import scala.math.Ordering.Implicits.infixOrderingOps

import common.json.Jsonable
import common.json.ToJsonableOps.{jsonifySingle, parseJsValue}

private class LastAlbums private (
    private val queue: Queue[AlbumDir],
    val lastUpdateTime: LocalDateTime,
) {
  def this(lastUpdateTime: LocalDateTime) = this(Queue.empty, lastUpdateTime)
  def enqueue(albumDir: AlbumDir): LastAlbums = {
    val modified = albumDir.dir.lastModifiedTime
    if (modified <= lastUpdateTime) this else new LastAlbums(queue.enqueue(albumDir), modified)
  }
  def enqueueAll(albumDirs: Seq[AlbumDir]): LastAlbums =
    albumDirs.sortBy(_.dir.lastModifiedTime).foldLeft(this)(_.enqueue(_))
  def dequeue: Option[(AlbumDir, LastAlbums)] = queue.dequeueOption.flatMap { case (album, tail) =>
    val newLastAlbums = new LastAlbums(tail, lastUpdateTime)
    if (album.dir.exists)
      Some((album, newLastAlbums))
    else {
      scribe.warn(s"Album directory ${album.dir} no longer exists, skipping")
      newLastAlbums.dequeue
    }
  }
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
