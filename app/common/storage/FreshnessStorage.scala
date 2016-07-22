package common.storage

import common.rich.RichT._
import org.joda.time.DateTime

import scala.concurrent.{ExecutionContext, Future}


class FreshnessStorage[Key, Value](storage: LocalStorage[Key, (Value, DateTime)])
    (implicit ec: ExecutionContext) extends LocalStorage[Key, Value] {
  private def now(v: Value): (Value, DateTime) = v -> DateTime.now
  private def toValue(v: Future[Option[(Value, DateTime)]]) = v.map(_.map(_._1))
  def freshness(k: Key): Future[Option[(Value, DateTime)]] = storage load k
  override def store(k: Key, v: Value) =
    storage.store(k, now(v))
  override def load(k: Key): Future[Option[Value]] =
    storage.load(k) |> toValue
  override def forceStore(k: Key, v: Value): Future[Option[Value]] =
    storage.forceStore(k, now(v)) |> toValue
}
