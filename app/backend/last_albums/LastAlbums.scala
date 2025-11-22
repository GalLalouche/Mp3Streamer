package backend.last_albums

import java.time.{Clock, LocalDateTime}

import models.AlbumDir
import play.api.libs.json.JsValue

import scala.collection.immutable.Queue
import scala.math.Ordering.Implicits.infixOrderingOps

import common.rich.func.TuplePLenses.__2

import common.io.JsonableSaver
import common.json.Jsonable
import common.json.ToJsonableOps.{jsonifySingle, parseJsValue}
import common.rich.RichTime.RichClock

private class LastAlbums(
    private val queue: Queue[AlbumDir],
    private val lastUpdateTime: LocalDateTime,
) {
  def enqueue(albumDir: AlbumDir): LastAlbums = {
    val modified = albumDir.dir.lastModified
    if (modified <= lastUpdateTime) this else new LastAlbums(queue.enqueue(albumDir), modified)
  }
  def dequeue: Option[(AlbumDir, LastAlbums)] =
    queue.dequeueOption.map(__2.modify(new LastAlbums(_, lastUpdateTime)))
  def persist(saver: JsonableSaver)(implicit ev: Jsonable[AlbumDir]): Unit =
    saver.saveObject(this)
  def albums: Seq[AlbumDir] = queue
}
private object LastAlbums {
  def load(saver: JsonableSaver, clock: Clock)(implicit ev: Jsonable[AlbumDir]): LastAlbums =
    saver.loadObjectOpt[LastAlbums].getOrElse(new LastAlbums(Queue.empty, clock.getLocalDateTime))

  private implicit def jsonableLastAlbums(implicit ev: Jsonable[AlbumDir]): Jsonable[LastAlbums] =
    new Jsonable[LastAlbums] {
      override def jsonify(e: LastAlbums): JsValue =
        (e.queue: Seq[AlbumDir], e.lastUpdateTime).jsonify
      override def parse(json: JsValue): LastAlbums = {
        val (dirs, lastUpdateTime) = json.parse[(Seq[AlbumDir], LocalDateTime)]
        new LastAlbums(Queue(dirs: _*), lastUpdateTime)
      }
    }
}
