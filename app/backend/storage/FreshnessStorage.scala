package backend.storage

import common.rich.RichT._
import org.joda.time.DateTime

import scala.concurrent.{ExecutionContext, Future}


/**
 * Keep a timestamp for every value. If the timestamp does not exist (but the value does), it means
 * that the value does not need to be updated
 */
class FreshnessStorage[Key, Value](storage: LocalStorage[Key, (Value, Option[DateTime])])
                                  (implicit ec: ExecutionContext) extends LocalStorage[Key, Value] {
  private def now(v: Value) = v -> Some(DateTime.now)
  private def toValue(v: Future[Option[(Value, Any)]]) = v.map(_.map(_._1))
  // 1st option: the data may not be there; 2nd option: it might be there but null
  def freshness(k: Key): Future[Option[Option[DateTime]]] = storage load k map (_ map (_._2))
  def storeWithoutTimestamp(k: Key, v: Value) = storage.store(k, v -> None)
  override def store(k: Key, v: Value) =
    storage.store(k, now(v))
  override def load(k: Key): Future[Option[Value]] =
    storage.load(k) |> toValue
  override def forceStore(k: Key, v: Value): Future[Option[Value]] =
    storage.forceStore(k, now(v)) |> toValue
  override def utils: LocalStorageUtils = storage.utils
}
