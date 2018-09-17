package backend.storage

import java.time.{Clock, LocalDateTime}

import backend.FutureOption
import backend.RichTime._
import common.rich.RichT._
import common.rich.func.TuplePLenses
import common.storage.Storage

import scala.concurrent.{ExecutionContext, Future}

/**
  * Keep a timestamp for every value. If the timestamp does not exist (but the value does), it means
  * that the value does not need to be updated.
  */
class FreshnessStorage[Key, Value](storage: Storage[Key, (Value, Option[LocalDateTime])], clock: Clock)
    (implicit ec: ExecutionContext)
    extends Storage[Key, Value] {
  private def now(v: Value): (Value, Option[LocalDateTime]) =
    v -> Some(clock.instant.toLocalDateTime)
  private def toValue(v: FutureOption[(Value, Any)]): FutureOption[Value] = v.map(_.map(_._1))
  // 1st option: the time data may not be there; 2nd option: it might be there but null
  // TODO replace with ADT
  def freshness(k: Key): FutureOption[Option[LocalDateTime]] = storage load k map (_ map (_._2))
  def storeWithoutTimestamp(k: Key, v: Value): Future[Unit] = storage.store(k, v -> None)
  override def store(k: Key, v: Value) = storage.store(k, v |> now)
  override def storeMultiple(kvs: Seq[(Key, Value)]) =
    storage storeMultiple kvs.map(TuplePLenses.tuple2Second modify now)
  override def load(k: Key) = storage.load(k) |> toValue
  override def forceStore(k: Key, v: Value) = storage.forceStore(k, v |> now) |> toValue
  // Also updates the timestamp to now
  override def mapStore(k: Key, f: Value => Value, default: => Value) =
    storage.mapStore(k, e => now(f(e._1)), default |> now) |> toValue
  override def delete(k: Key) = storage.delete(k).map(_.map(_._1))
  override def utils = storage.utils
}
