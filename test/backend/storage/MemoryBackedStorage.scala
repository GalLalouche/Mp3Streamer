package backend.storage

import scala.collection.mutable
import scala.concurrent.Future

import common.rich.func.BetterFutureInstances._
import common.rich.func.ToTransableOps.toHoistIdOps

import common.storage.{Storage, StoreMode}

class MemoryBackedStorage[Key, Value] extends Storage[Key, Value] {
  private val map = new mutable.HashMap[Key, Value]
  override def update(k: Key, v: Value) = map.put(k, v).hoistId
  override def replace(k: Key, v: Value) = map.put(k, v).hoistId
  override def store(k: Key, v: Value) =
    if (map contains k)
      Future.failed(new IllegalArgumentException(s"Key <$k> already exists"))
    else {
      replace(k, v)
      Future.successful(())
    }
  override def load(k: Key) = map.get(k).hoistId
  override def exists(k: Key) = Future.successful(map.contains(k))
  override def delete(k: Key) = map.remove(k).hoistId
  override def utils = ???
  override def mapStore(mode: StoreMode, k: Key, f: Value => Value, default: => Value) =
    replace(k, map.get(k).map(f).getOrElse(default))
  override def storeMultiple(kvs: Seq[(Key, Value)]) =
    if (kvs.map(_._1).exists(map.contains))
      Future.failed(new IllegalArgumentException("Found repeat keys"))
    else
      overwriteMultipleVoid(kvs)
  override def overwriteMultipleVoid(kvs: Seq[(Key, Value)]) =
    Future.successful(map ++= kvs)
}
