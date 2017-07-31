package backend.storage

import java.time.{Clock, LocalDateTime}

import backend.RichTime._
import common.rich.RichT._

import scala.concurrent.{ExecutionContext, Future}


/**
  * Keep a timestamp for every value. If the timestamp does not exist (but the value does), it means
  * that the value does not need to be updated.
  */
class FreshnessStorage[Key, Value](storage: Storage[Key, (Value, Option[LocalDateTime])])
    (implicit ec: ExecutionContext, clock: Clock)
    extends Storage[Key, Value] {
  private def now(v: Value): (Value, Option[LocalDateTime]) = v -> Some(clock.instant.toLocalDateTime)
  private def toValue(v: Future[Option[(Value, Any)]]): Future[Option[Value]] = v.map(_.map(_._1))
  // 1st option: the time data may not be there; 2nd option: it might be there but null
  def freshness(k: Key): Future[Option[Option[LocalDateTime]]] = storage load k map (_ map (_._2))
  def storeWithoutTimestamp(k: Key, v: Value): Future[Boolean] = storage.store(k, v -> None)
  override def store(k: Key, v: Value) = storage.store(k, v |> now)
  override def load(k: Key) = storage.load(k) |> toValue
  override def forceStore(k: Key, v: Value) = storage.forceStore(k, v |> now) |> toValue
  // Also updates the timestamp to now
  override def mapStore(k: Key, f: Value => Value, default: => Value) =
    storage.mapStore(k, e => now(f(e._1)), default |> now) |> toValue
  override def delete(k: Key) = storage.delete(k).map(_.map(_._1))
  override def utils = storage.utils
}
