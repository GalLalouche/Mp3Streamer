//package common.storage
//
//import org.joda.time.DateTime
//
//import scala.concurrent.{ExecutionContext, Future}
//
//class FreshnessStorage[Key, Value](storage: LocalStorage[Key, (Value, DateTime)])
//                                  (implicit ec: ExecutionContext) extends LocalStorage[Key, Value] {
//  def freshness(k: Key): Future[Option[(Value, DateTime)]] = storage load k
//  override def store(k: Key, v: Value): Future[Option[Value]] = ???
////    storage.store(k, v -> DateTime.now()).map(_.map(_._1))
//  override def load(k: Key): Future[Option[Value]] = storage.load(k).map(_.map(_._1))
//}
