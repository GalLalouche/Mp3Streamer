package backend.storage

import common.JodaClock
import common.rich.RichT._
import org.joda.time.DateTime

import scala.concurrent.{ExecutionContext, Future}


/**
 * Keep a timestamp for every value. If the timestamp does not exist (but the value does), it means
 * that the value does not need to be updated.
 */
class FreshnessStorage[Key, Value](storage: Storage[Key, (Value, Option[DateTime])])
                                  (implicit ec: ExecutionContext, clock: JodaClock)
    extends Storage[Key, Value] {
  private def now: DateTime = clock.now.toDateTime
  private def now(v: Value): (Value, Option[DateTime]) = v -> Some(now)
  private def toValue(v: Future[Option[(Value, Any)]]) = v.map(_.map(_._1))
  // 1st option: the data may not be there; 2nd option: it might be there but null
  def freshness(k: Key): Future[Option[Option[DateTime]]] = storage load k map (_ map (_._2))
  def storeWithoutTimestamp(k: Key, v: Value): Future[Boolean] = storage.store(k, v -> None)
  override def store(k: Key, v: Value) =
    storage.store(k, now(v))
  override def load(k: Key): Future[Option[Value]] =
    storage.load(k) |> toValue
  override def forceStore(k: Key, v: Value): Future[Option[Value]] =
    storage.forceStore(k, now(v)) |> toValue
  /** Also updates the timestamp */
  override def mapStore(k: Key, f: Value => Value, default: => Value): Future[Option[Value]] =
    storage.mapStore(k, e => now(f(e._1)), now(default)) |> toValue
  override def delete(k: Key): Future[Option[Value]] =
    storage.delete(k).map(_.map(_._1))
  override def utils: StorageUtils = storage.utils
}
