package backend.storage

import java.time.Clock

import backend.FutureOption
import backend.RichTime._
import common.rich.RichT._
import common.rich.func.TuplePLenses
import common.storage.Storage

import scala.concurrent.{ExecutionContext, Future}

import scalaz.OptionT
import scalaz.std.FutureInstances

/**
  * Keeps a timestamp for every value. If the timestamp does not exist (but the value does), it means that the
  * value does not need to be updated.
  */
class FreshnessStorage[Key, Value](storage: Storage[Key, (Value, Freshness)], clock: Clock)
    (implicit ec: ExecutionContext)
    extends Storage[Key, Value] with FutureInstances {
  private def now(v: Value): (Value, Freshness) = v -> DatedFreshness(clock.instant.toLocalDateTime)
  private def toValue(v: FutureOption[(Value, Any)]): FutureOption[Value] = OptionT(v).map(_._1).run
  def freshness(k: Key): FutureOption[Freshness] = OptionT(storage.load(k)).map(_._2).run
  def storeWithoutTimestamp(k: Key, v: Value): Future[Unit] = storage.store(k, v -> AlwaysFresh)
  override def store(k: Key, v: Value) = storage.store(k, v |> now)
  override def storeMultiple(kvs: Seq[(Key, Value)]) =
    storage storeMultiple kvs.map(TuplePLenses.tuple2Second modify now)
  override def load(k: Key) = storage.load(k) |> toValue
  override def forceStore(k: Key, v: Value) = storage.forceStore(k, v |> now) |> toValue
  // Also updates the timestamp to now
  override def mapStore(k: Key, f: Value => Value, default: => Value) =
    storage.mapStore(k, e => now(f(e._1)), default |> now) |> toValue
  override def delete(k: Key) = OptionT(storage.delete(k)).map(_._1).run
  override def utils = storage.utils
}
