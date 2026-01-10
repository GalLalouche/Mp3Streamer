package backend.storage

import java.time.Clock

import backend.FutureOption

import scala.concurrent.{ExecutionContext, Future}

import common.rich.RichT._
import common.rich.RichTime.RichInstant
import common.rich.RichTuple._
import common.storage.{Storage, StoreMode}

/**
 * Keeps a timestamp for every value. If the timestamp does not exist (but the value does), it means
 * that the value does not need to be updated.
 */
class ComposedFreshnessStorage[Key, Value](storage: Storage[Key, (Value, Freshness)], clock: Clock)(
    implicit ec: ExecutionContext,
) extends FreshnessStorage[Key, Value]
    with Storage[Key, Value] {
  private def now(v: Value): (Value, Freshness) = v -> DatedFreshness(clock.instant.toLocalDateTime)
  private def toValue[A](v: FutureOption[(Value, A)]): FutureOption[Value] = v.map(_._1)
  override def freshness(k: Key): FutureOption[Freshness] = storage.load(k).map(_._2)
  override def foreverFresh(k: Key, v: Value): Future[Unit] =
    storage.store(k, v -> AlwaysFresh)
  override def store(k: Key, v: Value) = storage.store(k, v |> now)
  override def storeMultiple(kvs: Iterable[(Key, Value)]) =
    storage storeMultiple kvs.map(_.modifySecond(now))
  override def overwriteMultipleVoid(kvs: Iterable[(Key, Value)]) =
    storage overwriteMultipleVoid kvs.map(_.modifySecond(now))
  override def load(k: Key) = storage.load(k) |> toValue
  override def exists(k: Key) = storage.exists(k)
  override def update(k: Key, v: Value) = storage.update(k, v |> now) |> toValue
  /** A utility when the value is [[Unit]]. Returns [[true]] if a value was updated. */
  def update(k: Key)(implicit ev: Unit =:= Value): Future[Boolean] =
    storage.update(k, now(ev(()))).value.map(_.isDefined)
  override def replace(k: Key, v: Value) = storage.replace(k, v |> now) |> toValue
  // Also updates the timestamp to now
  override def mapStore(mode: StoreMode, k: Key, f: Value => Value, default: => Value) =
    storage.mapStore(mode, k, e => now(f(e._1)), default |> now) |> toValue
  override def delete(k: Key) = storage.delete(k).map(_._1)
  override def deleteAll(keys: Iterable[Key]) = storage.deleteAll(keys)
  override def utils = storage.utils
}
