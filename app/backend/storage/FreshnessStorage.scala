package backend.storage

import java.time.Clock

import backend.FutureOption
import backend.RichTime._

import scala.concurrent.{ExecutionContext, Future}

import common.rich.func.BetterFutureInstances._

import common.rich.RichT._
import common.rich.RichTuple._
import common.storage.Storage

/**
  * Keeps a timestamp for every value. If the timestamp does not exist (but the value does), it means that the
  * value does not need to be updated.
  */
class FreshnessStorage[Key, Value](storage: Storage[Key, (Value, Freshness)], clock: Clock)
    (implicit ec: ExecutionContext) extends Storage[Key, Value] {
  private def now(v: Value): (Value, Freshness) = v -> DatedFreshness(clock.instant.toLocalDateTime)
  private def toValue[A](v: FutureOption[(Value, A)]): FutureOption[Value] = v.map(_._1)
  def freshness(k: Key): FutureOption[Freshness] = storage.load(k).map(_._2)
  def storeWithoutTimestamp(k: Key, v: Value): Future[Unit] = storage.store(k, v -> AlwaysFresh)
  override def store(k: Key, v: Value) = storage.store(k, v |> now)
  override def storeMultiple(kvs: Seq[(Key, Value)]) =
    storage storeMultiple kvs.map(_ modifySecond now)
  override def load(k: Key) = storage.load(k) |> toValue
  override def exists(k: Key) = storage.exists(k)
  override def forceStore(k: Key, v: Value) = storage.forceStore(k, v |> now) |> toValue
  // Also updates the timestamp to now
  override def mapStore(k: Key, f: Value => Value, default: => Value) =
    storage.mapStore(k, e => now(f(e._1)), default |> now) |> toValue
  override def delete(k: Key) = storage.delete(k).map(_._1)
  override def utils = storage.utils
}
