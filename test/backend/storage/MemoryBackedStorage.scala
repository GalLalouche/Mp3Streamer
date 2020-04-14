package backend.storage

import scala.collection.mutable
import scala.concurrent.Future

import common.rich.func.BetterFutureInstances._
import common.rich.func.RichOptionT

import common.storage.Storage

class MemoryBackedStorage[Key, Value] extends Storage[Key, Value] {
  private val map = new mutable.HashMap[Key, Value]
  override def forceStore(k: Key, v: Value) = RichOptionT.app[Future].apply(map.put(k, v))
  override def store(k: Key, v: Value) =
    if (map contains k)
      Future failed new IllegalArgumentException(s"Key <$k> already exists")
    else {
      forceStore(k, v)
      Future.successful()
    }
  override def load(k: Key) = RichOptionT.app[Future].apply(map.get(k))
  override def exists(k: Key) = Future.successful(map.contains(k))
  def get(k: Key): Option[Value] = map.get(k)
  override def delete(k: Key) = RichOptionT.app[Future].apply(map.remove(k))
  override def utils = ???
  override def mapStore(k: Key, f: Value => Value, default: => Value) =
    forceStore(k, map get k map f getOrElse default)
  override def storeMultiple(kvs: Seq[(Key, Value)]) =
    if (kvs.map(_._1).exists(map.contains))
      Future failed new IllegalArgumentException("Found repeat keys")
    else
      Future.successful(map ++= kvs)
}
